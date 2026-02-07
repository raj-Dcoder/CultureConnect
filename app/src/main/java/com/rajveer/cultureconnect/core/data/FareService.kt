package com.rajveer.cultureconnect.core.data

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.firebase.Firebase
import com.rajveer.cultureconnect.features.travel.FareResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/** Service to call Cloud Functions for fare calculation */
@Singleton
class FareService @Inject constructor() {

    private val functions: FirebaseFunctions = Firebase.functions

    companion object {
        private const val TAG = "FareService"
        private const val FUNCTION_NAME = "calculateFares"
    }

    /**
     * Calculate fares for multiple ride-sharing providers
     *
     * @param originLat Pickup latitude
     * @param originLng Pickup longitude
     * @param destLat Destination latitude
     * @param destLng Destination longitude
     * @return Result with FareResponse or error
     */
    suspend fun calculateFares(
            originLat: Double,
            originLng: Double,
            destLat: Double,
            destLng: Double
    ): Result<FareResponse> {
        return try {
            Log.d(TAG, "🚀 Calling Cloud Function: $FUNCTION_NAME")
            Log.d(TAG, "📍 Origin: ($originLat, $originLng)")
            Log.d(TAG, "📍 Destination: ($destLat, $destLng)")

            // Create request data
            val request =
                    hashMapOf(
                            "originLat" to originLat,
                            "originLng" to originLng,
                            "destLat" to destLat,
                            "destLng" to destLng
                    )

            // Call the Cloud Function
            val result = functions.getHttpsCallable(FUNCTION_NAME).call(request).await()

            // Parse the response
            val data = result.data as? Map<*, *> ?: throw Exception("Invalid response format")

            Log.d(TAG, "✅ Cloud Function response received")

            // Parse providers list
            val providersList =
                    (data["providers"] as? List<*>)?.mapNotNull { providerData ->
                        val provider = providerData as? Map<*, *> ?: return@mapNotNull null
                        com.rajveer.cultureconnect.features.travel.FareEstimate(
                                provider = provider["provider"] as? String ?: "",
                                category = provider["category"] as? String ?: "",
                                estimatedFare = (provider["estimatedFare"] as? Number)?.toInt()
                                                ?: 0,
                                currency = provider["currency"] as? String ?: "INR",
                                distance = provider["distance"] as? String ?: "",
                                duration = provider["duration"] as? String ?: "",
                                deepLink = provider["deepLink"] as? String ?: ""
                        )
                    }
                            ?: emptyList()

            // Parse distance
            val distanceData = data["distance"] as? Map<*, *>
            val distance =
                    com.rajveer.cultureconnect.features.travel.DistanceInfo(
                            value = (distanceData?.get("value") as? Number)?.toInt() ?: 0,
                            text = distanceData?.get("text") as? String ?: ""
                    )

            // Parse duration
            val durationData = data["duration"] as? Map<*, *>
            val duration =
                    com.rajveer.cultureconnect.features.travel.DurationInfo(
                            value = (durationData?.get("value") as? Number)?.toInt() ?: 0,
                            text = durationData?.get("text") as? String ?: ""
                    )

            val fareResponse =
                    FareResponse(
                            providers = providersList,
                            distance = distance,
                            duration = duration
                    )

            Log.d(TAG, "✅ Parsed ${providersList.size} provider estimates")
            Log.d(TAG, "📏 Distance: ${distance.text}, Duration: ${duration.text}")

            Result.success(fareResponse)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error calling Cloud Function", e)
            Result.failure(e)
        }
    }
}
