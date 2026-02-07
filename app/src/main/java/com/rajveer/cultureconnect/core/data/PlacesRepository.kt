package com.rajveer.cultureconnect.core.data

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.*
import com.rajveer.cultureconnect.features.travel.PlaceDetails
import com.rajveer.cultureconnect.features.travel.PlacePrediction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Google Places API interactions
 * 
 * Handles autocomplete predictions and place details fetching
 */
@Singleton
class PlacesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val placesClient: PlacesClient by lazy {
        Log.d(TAG, "🔧 Initializing Places API...")
        // Initialize Places API (only once)
        if (!Places.isInitialized()) {
            Log.d(TAG, "📦 Places not initialized, initializing now...")
            val apiKey = getApiKey()
            if (apiKey.isNotEmpty()) {
                Places.initialize(context, apiKey)
                Log.d(TAG, "✅ Places initialized successfully with API key")
            } else {
                Log.e(TAG, "❌ Cannot initialize Places - API key is empty!")
                throw IllegalStateException("API Key is required for Places SDK")
            }
        } else {
            Log.d(TAG, "✅ Places already initialized")
        }
        val client = Places.createClient(context)
        Log.d(TAG, "✅ PlacesClient created")
        client
    }
    
    // Session token for billing optimization
    private var sessionToken: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()
    
    /**
     * Get autocomplete predictions for a query
     * 
     * @param query User's search text
     * @param bias Optional location bias (search near this location)
     * @return List of place predictions
     */
    suspend fun getAutocompletePredictions(
        query: String,
        bias: LatLng? = null
    ): Result<List<PlacePrediction>> {
        Log.d(TAG, "🔍 getAutocompletePredictions called with query: '$query'")
        
        if (query.isBlank()) {
            Log.d(TAG, "❌ Query is blank, returning empty list")
            return Result.success(emptyList())
        }
        
        return try {
            Log.d(TAG, "🏗️ Building autocomplete request...")
            // Build autocomplete request
            val requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(sessionToken)
                .setQuery(query)
            
            // Add location bias if provided (search near Goa by default)
            bias?.let {
                Log.d(TAG, "📍 Adding location bias: ${it.latitude}, ${it.longitude}")
                val bounds = RectangularBounds.newInstance(
                    LatLng(it.latitude - 0.5, it.longitude - 0.5),  // Southwest
                    LatLng(it.latitude + 0.5, it.longitude + 0.5)   // Northeast
                )
                requestBuilder.setLocationBias(bounds)
            }

            val request = requestBuilder.build()
            Log.d(TAG, "📡 Making API call to Places API...")
            
            // Make API call
            val response = placesClient.findAutocompletePredictions(request).await()
            Log.d(TAG, "✅ API call successful! Received ${response.autocompletePredictions.size} predictions")
            
            // Convert to our model
            val predictions = response.autocompletePredictions.map { prediction ->
                PlacePrediction(
                    placeId = prediction.placeId,
                    primaryText = prediction.getPrimaryText(null).toString(),
                    secondaryText = prediction.getSecondaryText(null)?.toString(),
                    fullText = prediction.getFullText(null).toString()
                )
            }
            
            Log.d(TAG, "✅ Converted to ${predictions.size} PlacePrediction objects")
            Result.success(predictions)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in getAutocompletePredictions", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Get place details (including coordinates) for a place ID
     * 
     * @param placeId The place ID from autocomplete
     * @return Place details with coordinates
     */
    suspend fun getPlaceDetails(placeId: String): Result<PlaceDetails> {
        Log.d(TAG, "📍 getPlaceDetails called for placeId: $placeId")
        
        return try {
            // Specify which fields we need (to minimize billing)
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
            
            Log.d(TAG, "🏗️ Building place details request...")
            // Build request
            val request = FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(sessionToken)
                .build()
            
            Log.d(TAG, "📡 Making API call to fetch place details...")
            // Make API call
            val response = placesClient.fetchPlace(request).await()
            val place = response.place
            
            Log.d(TAG, "✅ Place details received: ${place.name}")
            
            // Reset session token (for billing - new session after place selection)
            sessionToken = AutocompleteSessionToken.newInstance()
            Log.d(TAG, "🔄 Session token reset")
            
            // Convert to our model
            val placeDetails = PlaceDetails(
                placeId = place.id ?: placeId,
                name = place.name ?: "Unknown",
                address = place.address ?: "",
                latLng = place.latLng ?: LatLng(0.0, 0.0)
            )
            
            Log.d(TAG, "✅ Returning PlaceDetails: ${placeDetails.name} at ${placeDetails.latLng}")
            Result.success(placeDetails)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in getPlaceDetails", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Get API key from AndroidManifest meta-data
     */
    private fun getApiKey(): String {
        Log.d(TAG, "🔑 Reading API key from AndroidManifest...")
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_META_DATA
            )
            val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
            
            if (apiKey.isNullOrEmpty()) {
                Log.e(TAG, "❌ API key is null or empty in AndroidManifest!")
                Log.e(TAG, "Make sure:")
                Log.e(TAG, "  1. MAPS_API_KEY is set in local.properties")
                Log.e(TAG, "  2. manifestPlaceholders is configured in build.gradle")
                Log.e(TAG, "  3. Project has been synced after adding the key")
                ""
            } else {
                // Show partial key for verification (security)
                Log.d(TAG, "✅ API key found: ${apiKey.take(10)}...${apiKey.takeLast(5)}")
                apiKey
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reading API key from manifest", e)
            ""
        }
    }
    
    companion object {
        private const val TAG = "PlacesRepository"
    }
}
