package com.rajveer.cultureconnect.core.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * Repository for handling all authentication operations.
 * 
 * Why Repository Pattern?
 * - Single source of truth for auth state
 * - Abstracts Firebase implementation details
 * - Easy to test (can mock this in tests)
 * - Easy to swap auth providers (Firebase → Supabase)
 */
@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    
    /**
     * Flow that emits current Firebase user whenever auth state changes.
     * 
     * How it works:
     * 1. callbackFlow creates a Flow from callbacks
     * 2. AuthStateListener listens to Firebase auth changes
     * 3. trySend emits new user to Flow
     * 4. awaitClose removes listener when Flow is cancelled
     * 
     * Why Flow instead of LiveData?
     * - Coroutine-based (modern Kotlin way)
     * - Composable (can use operators like map, filter)
     * - Works seamlessly with Compose
     */
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        // Create listener that fires whenever auth state changes
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            // Send current user (or null) to Flow
            trySend(auth.currentUser)
        }
        
        // Register the listener
        firebaseAuth.addAuthStateListener(authStateListener)
        
        // Cleanup: Remove listener when Flow is cancelled
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }
    
    /**
     * Sign in with Google using ID token.
     * 
     * @param idToken The Google ID token from Credential Manager
     * @return Result with FirebaseUser on success, Exception on failure
     * 
     * How Google Sign-In works:
     * 1. User selects Google account (handled by Credential Manager)
     * 2. Google returns ID token
     * 3. We send token to Firebase
     * 4. Firebase verifies token and creates/returns user
     */
suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
    return try {
        Log.d("AuthRepository", "🔵 Creating credential from ID token...")
        // Create Firebase credential from Google ID token
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        
        Log.d("AuthRepository", "🔵 Signing in to Firebase...")
        // Sign in to Firebase with the credential
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        
        // Return success with user
        val user = authResult.user ?: throw Exception("User is null after sign-in")
        Log.d("AuthRepository", "✅ Firebase sign-in successful! UID: ${user.uid}")
        Result.success(user)
    } catch (e: Exception) {
        Log.e("AuthRepository", "❌ Firebase sign-in failed: ${e.message}", e)
        Result.failure(e)
    }
}
    
    /**
     * Sign out the current user.
     * 
     * This will:
     * - Clear Firebase auth session
     * - Trigger authStateListener (currentUser Flow will emit null)
     * - UI will automatically update to show login screen
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current user synchronously (for one-time checks).
     * 
     * Use this when you need immediate user, not continuous updates.
     * For continuous updates, use currentUser Flow.
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}