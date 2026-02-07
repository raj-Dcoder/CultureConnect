package com.rajveer.cultureconnect.core.model

/**
 * Data class representing the complete user profile stored in Firestore.
 * All fields have default values to prevent crashes when deserializing data
 * from Firebase where fields might be missing (especially for new users).
 */
data class UserProfile(
    // Mandatory unique ID from Firebase Auth
    val uid: String = "",

    // User details from Google Sign-In
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",

    // Custom App-specific preferences
    val homeCity: String = "",
    val modePreference: String? = null, // e.g., "Explorer", "Contributor"
    val interests: List<String> = emptyList() // User's chosen cultural interests
)