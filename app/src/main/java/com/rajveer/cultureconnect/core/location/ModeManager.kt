package com.rajveer.cultureconnect.core.location

import com.rajveer.cultureconnect.core.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModeManager {

    // 1. The Mode State: Holds the current mode. We initialize it to "traveller" as a safe default.
    private val _mode = MutableStateFlow<String>("traveller")
    // This is the public, read-only Flow that the UI will collect from.
    val mode = _mode.asStateFlow()

    // 2. Update Logic: Takes the current city and user's profile to determine the mode.
    fun updateMode(currentCity: String, user: UserProfile) {

        // Trim and ignore case for robust comparison.
        val cleanedCurrentCity = currentCity.trim().lowercase()
        val cleanedHomeCity = user.homeCity.trim().lowercase()

        // Core Business Logic:
        val defaultMode = if (cleanedCurrentCity == cleanedHomeCity) {
            "local"
        } else {
            "traveller"
        }

        // We check if the user has a preferred mode set in their profile (e.g., they manually chose "local").
        // If they have a preference, we use that. Otherwise, we use the auto-detected 'defaultMode'.
        _mode.value = user.modePreference ?: defaultMode
    }
}