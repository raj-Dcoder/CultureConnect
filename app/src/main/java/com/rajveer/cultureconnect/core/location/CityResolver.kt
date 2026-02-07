package com.rajveer.cultureconnect.core.location

import android.content.Context
import android.location.Geocoder
import java.util.Locale

class CityResolver(private val context: Context) {

    fun getCityName(lat: Double, lng: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng, 1) ?: return ""

        return addresses.firstOrNull()?.locality ?: ""
    }
}