package com.rajveer.cultureconnect.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        private val savedRepo: SavedEventsRepository

) : ViewModel() {

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

    private val _savedIds = MutableStateFlow<List<String>>(emptyList())
    val savedIds = _savedIds.asStateFlow()

    fun loadSavedEvents() = viewModelScope.launch {
        _savedIds.value = savedRepo.getSavedEventIds()
    }

    /** Check and update location permission status */
    fun checkLocationPermission() {
        _hasLocationPermission.value = locationManager.hasLocationPermission()
    }

    /**
     * Main function to detect user mode based on current location.
     *
     * Flow:
     * 1. Check location permission
     * 2. Get current GPS coordinates
     * 3. Resolve city name from coordinates
     * 4. Update mode based on city comparison
     */
    fun detectUserMode() {
        viewModelScope.launch {
            try {
                _isDetecting.value = true
                _errorMessage.value = null

                // Step 1: Check permission
                if (!locationManager.hasLocationPermission()) {
                    _errorMessage.value = "Location permission not granted"
                    _isDetecting.value = false
                    return@launch
                }

                // Step 2: Get current location
                val location = locationManager.getCurrentLocation()
                if (location == null) {
                    _errorMessage.value =
                            "Unable to get location. Please check if location services are enabled."
                    _isDetecting.value = false
                    return@launch
                }

                val (lat, lng) = location
                Log.d("ProfileViewModel", "Location detected: $lat, $lng")

                // Step 3: Resolve city name
                val cityName = cityResolver.getCityName(lat, lng)
                if (cityName.isEmpty()) {
                    _errorMessage.value = "Unable to determine city name"
                    _isDetecting.value = false
                    return@launch
                }

                _detectedCity.value = cityName
                Log.d("ProfileViewModel", "City detected: $cityName")

                // Step 4: Update mode with mock user profile
                // TODO: Replace with actual user profile from Firestore
                val mockUserProfile =
                        UserProfile(
                                uid = "mock_user",
                                name = "Test User",
                                email = "test@example.com",
                                homeCity =
                                        "Jaipur", // 🔥 CHANGE THIS to your actual city for testing!
                                modePreference = null // null means auto-detect
                        )

                modeManager.updateMode(cityName, mockUserProfile)
                Log.d("ProfileViewModel", "Mode updated: ${mode.value}")

                _isDetecting.value = false
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error detecting mode", e)
                _errorMessage.value = "Error: ${e.message}"
                _isDetecting.value = false
            }
        }
    }

    /** Clear error message */
    fun clearError() {
        _errorMessage.value = null
    }

}
