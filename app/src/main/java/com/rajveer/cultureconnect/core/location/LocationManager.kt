package com.rajveer.cultureconnect.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await // Don't forget this import for coroutines!

class LocationManager(private val context: Context) {

    // 1. Client Setup: This is the main object we use to request location updates.
    private val client = LocationServices.getFusedLocationProviderClient(context)

    // 2. Permission Check: Crucial check before accessing location data.
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 3. Get Location: The main function to fetch the current location.
    @SuppressLint("MissingPermission") // Suppress lint warning because we check permissions inside the composable later
    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null

        // Priority_High_Accuracy: We want the best possible location for city detection!
        val task: Task<android.location.Location> = client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null // Cancellation token: keeping it simple for now
        )

        // The magic line! .await() converts the async Task into a suspend function result.
        val location = task.await() ?: return null

        return Pair(location.latitude, location.longitude)
    }
}