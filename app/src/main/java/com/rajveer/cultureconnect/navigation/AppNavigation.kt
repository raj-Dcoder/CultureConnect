package com.rajveer.cultureconnect.navigation

// Root level navigation routes
sealed class AppRoute(val route: String) {
    object Auth : AppRoute("auth")
    object Main : AppRoute("main")
}