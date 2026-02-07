package com.rajveer.cultureconnect.core.model

/**
 * Sealed class representing all possible authentication states.
 * 
 * Why sealed class?
 * - Type-safe: Compiler ensures we handle all cases
 * - Exhaustive when expressions: No need for 'else' branch
 * - Easy to add new states without breaking existing code
 */
sealed class AuthState {
    /**
     * Initial state when app starts.
     * We don't know yet if user is logged in or not.
     */
    object Loading : AuthState()
    
    /**
     * User is not logged in.
     * Show login screen.
     */
    object Unauthenticated : AuthState()
    
    /**
     * User is logged in but hasn't completed onboarding.
     * Show onboarding flow (home city, interests, etc.)
     */
    data class NeedsOnboarding(val firebaseUser: com.google.firebase.auth.FirebaseUser) : AuthState()
    
    /**
     * User is fully authenticated and onboarded.
     * Show main app.
     */
    data class Authenticated(val userProfile: UserProfile) : AuthState()
    
    /**
     * Something went wrong during authentication.
     * Show error message and retry option.
     */
    data class Error(val message: String) : AuthState()
}