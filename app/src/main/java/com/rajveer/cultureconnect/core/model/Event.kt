package com.rajveer.cultureconnect.core.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val areaName: String = "",
    val city: String = "Goa",
    val imageUrl: String = "",
    val isApproved: Boolean = true
)
