package com.rajveer.cultureconnect.core.location

import android.content.Context
import android.location.Geocoder
import java.util.Locale
import dagger.hilt.android.qualifiers.ApplicationContext  
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityResolver @Inject constructor(  
    @ApplicationContext private val context: Context  
){
    fun getCityName(lat: Double, lng: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng, 1) ?: return ""

        return addresses.firstOrNull()?.locality ?: ""
    }

    fun getFormattedAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng, 1) ?: return "Current Location"
    
        val address = addresses.firstOrNull() ?: return "Current Location"
    
        // Build address like: "Street Name, Locality"
        val parts = mutableListOf<String>()
    
        address.thoroughfare?.let { parts.add(it) }      // Street name
        address.locality?.let { parts.add(it) }          // City name
    
        return if (parts.isEmpty()) {
            "Current Location"
        } else {
            parts.joinToString(", ")
        }
    }
}