package com.rajveer.cultureconnect.core.model

data class Highlight(
    val id: String = "",
    val title: String = "",
    val shortText: String = "",
    val imageUrl: String = "",
    val tags: List<String> = emptyList(),
    val city: String = "Goa"
)
