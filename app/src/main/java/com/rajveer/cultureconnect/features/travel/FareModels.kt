
package com.rajveer.cultureconnect.features.travel

/**
 * Request data sent to Cloud Function
 */
data class FareRequest(
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double
)

/**
 * Single provider's fare estimate
 */
data class FareEstimate(
    val provider: String,           // "Ola Mini", "Uber Go", etc.
    val category: String,           // "Economy", "Premium", etc.
    val estimatedFare: Int,         // Fare in INR
    val currency: String,           // "INR"
    val distance: String,           // "5.2 km"
    val duration: String,           // "15 mins"
    val deepLink: String            // URL to open provider app
)

/**
 * Distance information
 */
data class DistanceInfo(
    val value: Int,                 // Distance in meters
    val text: String                // "5.2 km"
)

/**
 * Duration information
 */
data class DurationInfo(
    val value: Int,                 // Duration in seconds
    val text: String                // "15 mins"
)

/**
 * Complete response from Cloud Function
 */
data class FareResponse(
    val providers: List<FareEstimate>,
    val distance: DistanceInfo,
    val duration: DurationInfo
)
