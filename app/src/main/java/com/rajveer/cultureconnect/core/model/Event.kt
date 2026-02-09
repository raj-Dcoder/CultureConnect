package com.rajveer.cultureconnect.core.model

/**
 * Event data model representing a cultural event, festival, concert, or happening.
 * 
 * All fields have default values for safe Firestore deserialization.
 * When Firestore document is missing a field, it uses the default instead of crashing.
 */
data class Event(
    // Core identification
    val id: String = "",
    val title: String = "",
    val description: String = "",
    
    // Timing (timestamps in milliseconds)
    val startAt: Long = 0L,  // Unix timestamp: System.currentTimeMillis()
    val endAt: Long = 0L,
    
    // Location details
    val areaName: String = "",         // Human-readable: "Vagator Beach"
    val city: String = "Bhubaneswar",
    val latitude: Double = 0.0,        // GPS coordinates for map pin
    val longitude: Double = 0.0,
    
    // Visual & metadata
    val imageUrl: String = "",         // Event photo URL from Firebase Storage
    val category: String = "",         // Primary category: "Music", "Food", "Cultural", "Festival"
    val tags: List<String> = emptyList(), // Additional attributes: ["Tourist Friendly", "Family", "Nightlife"]
    
    // Moderation
    val isApproved: Boolean = true     // For Phase 4: Event approval system
)
