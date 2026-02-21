package com.rajveer.cultureconnect.core.design

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rajveer.cultureconnect.core.model.AuthState
import com.rajveer.cultureconnect.core.model.UserProfile
import com.rajveer.cultureconnect.features.auth.AuthScreen
import com.rajveer.cultureconnect.features.auth.AuthViewModel
import com.rajveer.cultureconnect.features.events.EventDetailScreen
import com.rajveer.cultureconnect.features.events.EventsScreen
import com.rajveer.cultureconnect.features.explore.ExploreDetailScreen
import com.rajveer.cultureconnect.features.explore.ExploreScreen
import com.rajveer.cultureconnect.features.onboarding.OnboardingScreen
import com.rajveer.cultureconnect.features.profile.ProfileScreen
import com.rajveer.cultureconnect.features.travel.TravelScreen
import com.rajveer.cultureconnect.navigation.BottomNavItem

/**
 * Root composable for the entire app.
 * 
 * Responsibilities:
 * - Observe auth state from AuthViewModel
 * - Show appropriate screen based on auth state
 * - Handle navigation between auth/onboarding/main app
 * 
 * Why this approach?
 * - Single source of truth (AuthViewModel)
 * - Automatic UI updates when auth state changes
 * - Clean separation of concerns
 */
@Composable
fun CultureConnectAppRoot() {
    // Get AuthViewModel (Hilt will inject dependencies)
    val authViewModel: AuthViewModel = hiltViewModel()
    
    // Observe auth state
    val authState by authViewModel.authState.collectAsState()
    
    // Show different screens based on auth state
    when (authState) {
        is AuthState.Loading -> {
            // Show loading while checking auth state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is AuthState.Unauthenticated -> {
            // User not logged in → Show login screen
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    // Navigation handled by AuthScreen's LaunchedEffect
                },
                onNeedsOnboarding = {
                    // Navigation handled by AuthScreen's LaunchedEffect
                }
            )
        }
        
        is AuthState.NeedsOnboarding -> {
            // User logged in but needs onboarding
            val firebaseUser = (authState as AuthState.NeedsOnboarding).firebaseUser
            OnboardingScreen(
                viewModel = authViewModel,
                firebaseUser = firebaseUser
            )
        }
        
        is AuthState.Authenticated -> {
            // User fully authenticated → Show main app
            val userProfile = (authState as AuthState.Authenticated).userProfile
            MainAppWithBottomNav(
                userProfile = userProfile,
                onLogout = {
                    authViewModel.signOut()
                }
            )
        }
        
        is AuthState.Error -> {
            // Show error screen (you can improve this later)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${(authState as AuthState.Error).message}")
            }
        }
    }
}

/**
 * Main app with bottom navigation.
 * Only shown when user is authenticated.
 */
@Composable
private fun MainAppWithBottomNav(
    userProfile: UserProfile,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    // Create ProfileViewModel early so location detects on app launch, not on Profile visit
    val profileViewModel: com.rajveer.cultureconnect.features.profile.ProfileViewModel = hiltViewModel()
    val items = listOf(
        BottomNavItem.Explore,
        BottomNavItem.Events,
        BottomNavItem.Travel,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination
                items.forEach { item ->
                    val selected = currentDestination?.route == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painterResource(id = item.iconRes),
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Explore.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Explore.route) {
                ExploreScreen(
                    onEventClick = { id -> navController.navigate("event/$id") },
                    onExploreItemClick = { id -> navController.navigate("explore/$id") }
                )
            }
            composable("events") {
                EventsScreen(onEventClick = { id -> navController.navigate("event/$id") })
            }
            composable("event/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")!!
                EventDetailScreen(
                    eventId = id,
                    onBack = { navController.popBackStack() },
                    onLetsGo = { destination ->
                        navController.navigate("travel/go?destination=${Uri.encode(destination)}")
                    }
                )
            }
            composable(BottomNavItem.Travel.route) { TravelScreen() }
            composable("travel/go?destination={destination}") { backStack ->
                val destination = backStack.arguments?.getString("destination") ?: ""
                TravelScreen(destination = destination)
            }
            composable("explore/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")!!
                ExploreDetailScreen(
                    itemId = id,
                    onBack = { navController.popBackStack() },
                    onLetsGo = { destination ->
                        navController.navigate("travel/go?destination=${Uri.encode(destination)}")
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onLogoutClick = onLogout,
                    onEventClick = { eventId ->
                        navController.navigate("event/$eventId")
                    }
                )
            }
        }
    }
}