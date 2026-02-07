package com.rajveer.cultureconnect.navigation

import androidx.annotation.DrawableRes

sealed class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    object Explore : BottomNavItem("explore", "Explore", android.R.drawable.ic_menu_compass)
    object Events  : BottomNavItem("events", "Events", android.R.drawable.ic_menu_today)
    object Travel  : BottomNavItem("travel", "Travel", android.R.drawable.ic_menu_directions)
    object Profile : BottomNavItem("profile", "Profile", android.R.drawable.ic_menu_myplaces)
}
