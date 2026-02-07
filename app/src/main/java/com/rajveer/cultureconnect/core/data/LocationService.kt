package com.rajveer.cultureconnect.core.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for getting device location using FusedLocationProvider
 * 
 * Uses Hilt for dependency injection
 */
@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * Get current location with high accuracy
     * 
     * @return Location object with lat/lng, or null if failed
     * @throws SecurityException if permission not granted (caller must check)
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            // Create cancellation token (for timeout)
            val cancellationTokenSource = CancellationTokenSource()
            
            // Request current location with high accuracy
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await() // Suspend until location is fetched
            
        } catch (e: Exception) {
            // Location failed (GPS off, timeout, etc.)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get last known location (faster but may be outdated)
     * 
     * @return Last known location, or null if none available
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}