package com.rajveer.cultureconnect.features.auth

import android.util.Log
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rajveer.cultureconnect.core.data.AuthRepository
import com.rajveer.cultureconnect.core.data.UserRepository
import com.rajveer.cultureconnect.core.model.AuthState
import com.rajveer.cultureconnect.core.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication flow.
 * 
 * Responsibilities:
 * - Manage auth state (Loading, Authenticated, Unauthenticated, etc.)
 * - Handle Google Sign-In
 * - Check onboarding status
 * - Coordinate between AuthRepository and UserRepository
 * 
 * Why ViewModel?
 * - Survives configuration changes (screen rotation)
 * - Manages UI state
 * - Handles business logic
 * - Separates UI from data layer
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    // Private mutable state (only ViewModel can change)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    
    // Public read-only state (UI observes this)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // Start observing auth state when ViewModel is created
        observeAuthState()
    }
    
    /**
     * Observe Firebase auth state changes.
     * 
     * This is the heart of the auth system:
     * 1. Listen to authRepository.currentUser Flow
     * 2. When user changes (sign in/out), update authState
     * 3. Check if user needs onboarding
     * 4. Fetch user profile if onboarded
     * 
     * Why in init?
     * - Starts immediately when ViewModel is created
     * - Runs in viewModelScope (cancelled when ViewModel dies)
     * - Ensures UI always has current auth state
     */
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { firebaseUser ->
                Log.d("AuthViewModel", "🔄 Auth state changed: ${firebaseUser?.email ?: "null"}")

                if (firebaseUser == null) {
                    // No user logged in
                    _authState.value = AuthState.Unauthenticated
                } else {
                    // User is logged in, check onboarding status
                    checkOnboardingStatus(firebaseUser.uid)
                }
            }
        }
    }
    
    /**
     * Check if user has completed onboarding.
     * 
     * @param uid User ID
     * 
     * Flow:
     * 1. Check if profile exists in Firestore
     * 2. If yes → fetch profile → AuthState.Authenticated
     * 3. If no → AuthState.NeedsOnboarding
     */
    private suspend fun checkOnboardingStatus(uid: String) {
        val hasOnboarded = userRepository.hasCompletedOnboarding(uid)
        
        if (hasOnboarded) {
            // User has completed onboarding, fetch full profile
            val profileResult = userRepository.getUserProfile(uid)
            
            profileResult.onSuccess { profile ->
                if (profile != null) {
                    _authState.value = AuthState.Authenticated(profile)
                } else {
                    // Profile should exist but doesn't (edge case)
                    _authState.value = AuthState.Error("Profile not found")
                }
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Failed to load profile")
            }
        } else {
            // User needs to complete onboarding
            val firebaseUser = authRepository.getCurrentUser()
            if (firebaseUser != null) {
                _authState.value = AuthState.NeedsOnboarding(firebaseUser)
            } else {
                _authState.value = AuthState.Error("User not found")
            }
        }
    }
    
    /**
     * Handle Google Sign-In.
     * 
     * @param idToken Google ID token from Credential Manager
     * 
     * Flow:
     * 1. Call authRepository.signInWithGoogle(idToken)
     * 2. If success → observeAuthState() will handle the rest
     * 3. If failure → show error
     * 
     * Why don't we manually update state here?
     * - observeAuthState() is already listening to currentUser Flow
     * - When sign-in succeeds, Firebase emits new user
     * - observeAuthState() catches it and updates state
     * - This keeps state management centralized
     */
    fun signInWithGoogle(idToken: String) {
    viewModelScope.launch {
        Log.d("AuthViewModel", "🔵 Starting Google sign-in...")
        Log.d("AuthViewModel", "ID Token length: ${idToken.length}")
        _authState.value = AuthState.Loading
        
        val result = authRepository.signInWithGoogle(idToken)
        
        result.onSuccess { user ->
            Log.d("AuthViewModel", "✅ Sign-in successful! User: ${user.email}")
        }.onFailure { exception ->
            Log.e("AuthViewModel", "❌ Sign-in failed: ${exception.message}", exception)
            _authState.value = AuthState.Error(
                exception.message ?: "Sign-in failed"
            )
        }
        // On success, observeAuthState() will handle state update
    }
}
    
    /**
     * Sign out current user.
     * 
     * Flow:
     * 1. Call authRepository.signOut()
     * 2. Firebase clears session
     * 3. observeAuthState() receives null user
     * 4. State becomes Unauthenticated
     * 5. UI shows login screen
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            // observeAuthState() will update state to Unauthenticated
        }
    }
    
    /**
     * Complete onboarding by creating user profile.
     * 
     * @param homeCity User's home city
     * @param interests User's interests
     * 
     * Called from onboarding screen when user completes setup.
     */
    fun completeOnboarding(homeCity: String, interests: List<String>) {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser()
            
            if (firebaseUser != null) {
                // Create user profile
                val userProfile = UserProfile(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    homeCity = homeCity,
                    interests = interests,
                    modePreference = null // Will be auto-detected
                )
                
                val result = userRepository.createUserProfile(userProfile)
                
                result.onSuccess {
                    // Profile created, update state
                    _authState.value = AuthState.Authenticated(userProfile)
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Failed to create profile"
                    )
                }
            } else {
                _authState.value = AuthState.Error("No user logged in")
            }
        }
    }
}