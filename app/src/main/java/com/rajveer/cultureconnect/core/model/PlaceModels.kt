package com.rajveer.cultureconnect.features.travel

import com.google.android.gms.maps.model.LatLng

/** Represents a place prediction from autocomplete */
data class PlacePrediction(
    val placeId: String,
    val primaryText: String, // Main text (e.g., "Baga Beach")
    val secondaryText: String?, // Secondary text (e.g., "Goa, India")
    val fullText: String // Combined text
)

/** Represents a selected place with coordinates */
data class PlaceDetails(
    val placeId: String,
    val name: String,
    val address: String,
    val latLng: LatLng
)

/** Represents a location (either from search or current location) */
data class TravelLocation(
    val name: String,
    val latLng: LatLng,
    val isCurrentLocation: Boolean = false
)
