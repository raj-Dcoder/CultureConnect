package com.rajveer.cultureconnect.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rajveer.cultureconnect.core.data.SavedEventsRepository
import com.rajveer.cultureconnect.core.location.CityResolver
import com.rajveer.cultureconnect.core.location.LocationManager
import com.rajveer.cultureconnect.core.location.ModeManager
import com.rajveer.cultureconnect.core.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    private val locationManager: LocationManager,
    private val cityResolver: CityResolver,
    private val modeManager: ModeManager,
    private val savedRepo: SavedEventsRepository,
    private val auth: FirebaseAuth
) : ViewModel() {


    // Current user info from Firebase Auth
    val userName: String get() = auth.currentUser?.displayName ?: "User"
    val userEmail: String get() = auth.currentUser?.email ?: ""
    val userPhotoUrl: String get() = auth.currentUser?.photoUrl?.toString() ?: ""

    // Expose mode from ModeManager
    val mode = modeManager.mode

    // State for detected city
    private val _detectedCity = MutableStateFlow<String>("")
    val detectedCity = _detectedCity.asStateFlow()

    // Loading state
    private val _isDetecting = MutableStateFlow(false)
    val isDetecting = _isDetecting.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Permission state
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission = _hasLocationPermission.asStateFlow()

    // Prevents auto-detection from running on every screen visit
    private var hasDetected = false

    private val _savedIds = MutableStateFlow<List<String>>(emptyList())
    val savedIds = _savedIds.asStateFlow()

    init {
        initIfNeeded() // Detect location as soon as app launches
    }

    /** Called by Screen's LaunchedEffect — only runs auto-detect ONCE per session */
    fun initIfNeeded() {
        checkLocationPermission()
        if (!hasDetected && _hasLocationPermission.value) {
            hasDetected = true
            detectUserMode()
        }
        loadSavedEvents()
    }

    fun loadSavedEvents() = viewModelScope.launch {
        try {
            _savedIds.value = savedRepo.getSavedEventIds()
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error loading saved events", e)
        }
    }

    fun clearSavedEvents() = viewModelScope.launch {
        try {
            savedRepo.clearAll()
            _savedIds.value = emptyList()
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error clearing saved events", e)
        }
    }

    fun removeSavedEvent(eventId: String) = viewModelScope.launch {
        try {
            savedRepo.removeEvent(eventId)
            _savedIds.value = _savedIds.value - eventId
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error removing saved event", e)
        }
    }

    fun checkLocationPermission() {
        _hasLocationPermission.value = locationManager.hasLocationPermission()
    }

    fun detectUserMode() {
        viewModelScope.launch {
            try {
                _isDetecting.value = true
                _errorMessage.value = null

                if (!locationManager.hasLocationPermission()) {
                    _errorMessage.value = "Location permission not granted"
                    _isDetecting.value = false
                    return@launch
                }

                val location = locationManager.getCurrentLocation()
                if (location == null) {
                    _errorMessage.value = "Unable to get location. Check if location services are enabled."
                    _isDetecting.value = false
                    return@launch
                }

                val (lat, lng) = location
                Log.d("ProfileViewModel", "Location detected: $lat, $lng")

                val cityName = cityResolver.getCityName(lat, lng)
                if (cityName.isEmpty()) {
                    _errorMessage.value = "Unable to determine city name"
                    _isDetecting.value = false
                    return@launch
                }

                _detectedCity.value = cityName
                Log.d("ProfileViewModel", "City detected: $cityName")

                // Use actual user data from FirebaseAuth
                val currentUser = auth.currentUser
                val userProfile = UserProfile(
                    uid = currentUser?.uid ?: "",
                    name = currentUser?.displayName ?: "",
                    email = currentUser?.email ?: "",
                    photoUrl = currentUser?.photoUrl?.toString() ?: "",
                    homeCity = "Bhubaneswar"
                )

                modeManager.updateMode(cityName, userProfile)
                Log.d("ProfileViewModel", "Mode updated: ${mode.value}")

                _isDetecting.value = false
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error detecting mode", e)
                _errorMessage.value = "Error: ${e.message}"
                _isDetecting.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
