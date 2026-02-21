package com.rajveer.cultureconnect.features.travel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.rajveer.cultureconnect.core.data.FareService
import com.rajveer.cultureconnect.core.data.LocationService
import com.rajveer.cultureconnect.core.data.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.rajveer.cultureconnect.core.location.CityResolver

/**
 * ViewModel for Travel feature - Ride fare comparison
 *
 * Manages state for location search, current location, and fare comparison
 */
@HiltViewModel
class TravelViewModel
@Inject
constructor(
        val locationService: LocationService,
        private val placesRepository: PlacesRepository,
        private val fareService: FareService,
        private val cityResolver: CityResolver
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<TravelUiState>(TravelUiState.Idle)
    val uiState: StateFlow<TravelUiState> = _uiState.asStateFlow()

    // From location
    private val _fromLocation = MutableStateFlow<TravelLocation?>(null)
    val fromLocation: StateFlow<TravelLocation?> = _fromLocation.asStateFlow()

    // To location
    private val _toLocation = MutableStateFlow<TravelLocation?>(null)
    val toLocation: StateFlow<TravelLocation?> = _toLocation.asStateFlow()

    // Search query for autocomplete
    private val _searchQuery = MutableStateFlow("")

    // Autocomplete predictions
    private val _predictions = MutableStateFlow<List<PlacePrediction>>(emptyList())
    val predictions: StateFlow<List<PlacePrediction>> = _predictions.asStateFlow()

    // Loading state for predictions
    private val _isPredictionsLoading = MutableStateFlow(false)
    val isPredictionsLoading: StateFlow<Boolean> = _isPredictionsLoading.asStateFlow()

    // Fare results
    private val _fareResults = MutableStateFlow<FareResponse?>(null)
    val fareResults: StateFlow<FareResponse?> = _fareResults.asStateFlow()

    // Loading state for fare calculation
    private val _isFareLoading = MutableStateFlow(false)
    val isFareLoading: StateFlow<Boolean> = _isFareLoading.asStateFlow()

    init {
        Log.d(TAG, "✅ TravelViewModel initialized")
        setupDebouncedSearch()
    }

    /**
     * Setup debounced search for autocomplete Waits 300ms after user stops typing before making API
     * call
     */
    @OptIn(FlowPreview::class)
    private fun setupDebouncedSearch() {
        Log.d(TAG, "🔧 Setting up debounced search")
        viewModelScope.launch {
            _searchQuery
                    .debounce(300) // Wait 300ms after last character
                    .distinctUntilChanged() // Only if query actually changed
                    .collect { query ->
                        Log.d(TAG, "📝 Debounced query collected: '$query'")
                        if (query.isBlank()) {
                            Log.d(TAG, "❌ Query is blank, clearing predictions")
                            _predictions.value = emptyList()
                            _isPredictionsLoading.value = false
                        } else {
                            Log.d(TAG, "🔍 Triggering search for: '$query'")
                            searchPlaces(query)
                        }
                    }
        }
    }

    /**
     * Swap pickup and drop locations
     */
    fun swapLocations() {
        // Save current values in temporary variables
        val tempFrom = _fromLocation.value
        val tempTo = _toLocation.value
        
        // Swap them
        _fromLocation.value = tempTo
        _toLocation.value = tempFrom
    }

    /** Update search query (triggers debounced search) */
    fun onSearchQueryChanged(query: String) {
        Log.d(TAG, "⌨️ onSearchQueryChanged called with: '$query'")
        _searchQuery.value = query
        if (query.isNotBlank()) {
            Log.d(TAG, "⏳ Setting predictions loading to true")
            _isPredictionsLoading.value = true
        }
    }

    /** Search for places using autocomplete */
    private suspend fun searchPlaces(query: String) {
        Log.d(TAG, "🌍 searchPlaces called with query: '$query'")
        // Use current location or Goa as bias
        val bias = _fromLocation.value?.latLng ?: LatLng(15.2993, 74.1240) // Goa center
        Log.d(TAG, "📍 Using bias location: ${bias.latitude}, ${bias.longitude}")

        Log.d(TAG, "📡 Calling PlacesRepository.getAutocompletePredictions...")
        placesRepository
                .getAutocompletePredictions(query, bias)
                .fold(
                        onSuccess = { predictions ->
                            Log.d(TAG, "✅ SUCCESS: Received ${predictions.size} predictions")
                            predictions.forEachIndexed { index, pred ->
                                Log.d(TAG, "  [$index] ${pred.primaryText} - ${pred.secondaryText}")
                            }
                            _predictions.value = predictions
                            _isPredictionsLoading.value = false
                        },
                        onFailure = { error ->
                            Log.e(TAG, "❌ FAILURE: Error getting predictions", error)
                            Log.e(TAG, "Error message: ${error.message}")
                            Log.e(TAG, "Error type: ${error.javaClass.simpleName}")
                            _predictions.value = emptyList()
                            _isPredictionsLoading.value = false
                            _uiState.value = TravelUiState.Error("Search failed: ${error.message}")
                        }
                )
    }

    /** User selected a place from predictions */
    fun onPlaceSelected(prediction: PlacePrediction, isFromLocation: Boolean) {
        Log.d(TAG, "👆 onPlaceSelected: ${prediction.primaryText} (isFrom: $isFromLocation)")
        viewModelScope.launch {
            _isPredictionsLoading.value = true

            Log.d(TAG, "📡 Fetching place details for placeId: ${prediction.placeId}")
            placesRepository
                    .getPlaceDetails(prediction.placeId)
                    .fold(
                            onSuccess = { placeDetails ->
                                Log.d(
                                        TAG,
                                        "✅ SUCCESS: Got place details - ${placeDetails.name} at ${placeDetails.latLng}"
                                )
                                val location =
                                        TravelLocation(
                                                name = placeDetails.name,
                                                latLng = placeDetails.latLng,
                                                isCurrentLocation = false
                                        )

                                if (isFromLocation) {
                                    Log.d(TAG, "📌 Setting FROM location")
                                    _fromLocation.value = location
                                } else {
                                    Log.d(TAG, "📌 Setting TO location")
                                    _toLocation.value = location
                                }

                                // Clear predictions
                                _predictions.value = emptyList()
                                _searchQuery.value = ""
                                _isPredictionsLoading.value = false
                            },
                            onFailure = { error ->
                                Log.e(TAG, "❌ FAILURE: Error getting place details", error)
                                _isPredictionsLoading.value = false
                                _uiState.value =
                                        TravelUiState.Error(
                                                "Failed to get location details: ${error.message}"
                                        )
                            }
                    )
        }
    }

    /** Get current location and set as "From" */
    fun useCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = TravelUiState.LoadingLocation

            // 👇 NEW: Check if location is enabled first
            if (!locationService.isLocationEnabled()) {
                _uiState.value = TravelUiState.Error(
                        "Location is turned off. Please enable GPS in your phone settings."
                )
                return@launch  // Exit early
            }

            val location = locationService.getCurrentLocation()

            if (location != null) {
                 val addressName = try {
                // You'll need to inject CityResolver into ViewModel
                cityResolver.getFormattedAddress(
                    location.latitude, 
                    location.longitude
                )
            } catch (e: Exception) {
                "Current Location"  // Fallback if geocoding fails
            }
                _fromLocation.value =
                        TravelLocation(
                                name = addressName,
                                latLng = LatLng(location.latitude, location.longitude),
                                isCurrentLocation = true
                        )
                _uiState.value = TravelUiState.Idle
            } else {
                _uiState.value = TravelUiState.Error("Failed to get current location")
            }
        }
    }

    /** Pre-fill destination from Let's Go button and auto-set current location as From */
    fun setDestination(destinationName: String) {
        Log.d(TAG, "📍 Setting destination: $destinationName")
        viewModelScope.launch {
            // Forward geocode the name to get real coordinates
            val coords = cityResolver.getCoordinatesFromName(destinationName)
            if (coords != null) {
                Log.d(TAG, "📍 Geocoded: $destinationName → (${coords.first}, ${coords.second})")
                _toLocation.value = TravelLocation(
                    name = destinationName,
                    latLng = LatLng(coords.first, coords.second),
                    isCurrentLocation = false
                )
            } else {
                Log.w(TAG, "⚠️ Geocoding failed for: $destinationName, using name only")
                _toLocation.value = TravelLocation(
                    name = destinationName,
                    latLng = LatLng(0.0, 0.0),
                    isCurrentLocation = false
                )
            }
            // Auto-fill "From" with current location
            if (_fromLocation.value == null) {
                useCurrentLocation()
            }
        }
    }

    /** Clear a location */
    fun clearLocation(isFromLocation: Boolean) {
        if (isFromLocation) {
            _fromLocation.value = null
        } else {
            _toLocation.value = null
        }
    }

    /** Compare fares by calling Cloud Function */
    fun compareFares() {
        val from = _fromLocation.value
        val to = _toLocation.value

        if (from == null || to == null) {
            _uiState.value = TravelUiState.Error("Please select both locations")
            return
        }

        Log.d(TAG, "🚗 Comparing fares...")
        Log.d(TAG, "📍 From: ${from.name} (${from.latLng.latitude}, ${from.latLng.longitude})")
        Log.d(TAG, "📍 To: ${to.name} (${to.latLng.latitude}, ${to.latLng.longitude})")

        viewModelScope.launch {
            _isFareLoading.value = true
            _uiState.value = TravelUiState.LoadingFares

            val result =
                    fareService.calculateFares(
                            originLat = from.latLng.latitude,
                            originLng = from.latLng.longitude,
                            destLat = to.latLng.latitude,
                            destLng = to.latLng.longitude
                    )

            _isFareLoading.value = false

            result.fold(
                    onSuccess = { fareResponse ->
                        Log.d(TAG, "✅ Received ${fareResponse.providers.size} fare estimates")
                        _fareResults.value = fareResponse
                        _uiState.value = TravelUiState.FareResultsReady(fareResponse)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Error getting fares", error)
                        _uiState.value =
                                TravelUiState.Error(
                                        "Failed to get fare estimates: ${error.message}"
                                )
                    }
            )
        }
    }

    companion object {
        private const val TAG = "TravelViewModel"
    }
}

/** UI State for Travel Screen */
sealed class TravelUiState {
    object Idle : TravelUiState()
    object LoadingLocation : TravelUiState()
    object LoadingFares : TravelUiState()
    data class FareResultsReady(val fareResponse: FareResponse) : TravelUiState()
    data class Success(val message: String) : TravelUiState()
    data class Error(val message: String) : TravelUiState()
}
