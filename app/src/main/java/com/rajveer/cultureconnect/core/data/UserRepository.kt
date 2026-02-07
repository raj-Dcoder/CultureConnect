package com.rajveer.cultureconnect.core.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rajveer.cultureconnect.core.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user profile operations in Firestore.
 * 
 * Responsibilities:
 * - Create user profile on first sign-in
 * - Fetch user profile
 * - Update user profile
 * - Check onboarding status
 */
@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    
    // Firestore collection reference
    private val usersCollection = firestore.collection("users")
    
    /**
     * Get user profile from Firestore.
     * 
     * @param uid User ID (Firebase Auth UID)
     * @return Result with UserProfile on success, Exception on failure
     * 
     * How it works:
     * 1. Query Firestore for document at users/{uid}
     * 2. Convert Firestore document to UserProfile data class
     * 3. Return result
     */
    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val document = usersCollection.document(uid).get().await()
            
            // Convert Firestore document to UserProfile
            // toObject() automatically maps fields to data class properties
            val userProfile = document.toObject(UserProfile::class.java)
            
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a new user profile in Firestore.
     * Called after first-time Google Sign-In.
     * 
     * @param userProfile The user profile to create
     * @return Result with Unit on success, Exception on failure
     * 
     * Why create profile separately from auth?
     * - Firebase Auth only stores basic info (email, name, photo)
     * - We need custom fields (homeCity, interests, modePreference)
     * - Firestore gives us flexibility to add more fields later
     */
    suspend fun createUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            // Set document at users/{uid} with userProfile data
            // set() overwrites existing document or creates new one
            usersCollection.document(userProfile.uid).set(userProfile).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update existing user profile.
     * 
     * @param userProfile Updated user profile
     * @return Result with Unit on success, Exception on failure
     * 
     * Difference between set() and update():
     * - set(): Overwrites entire document
     * - update(): Only updates specified fields
     * 
     * We use set() here to replace entire profile.
     */
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(userProfile.uid).set(userProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if user has completed onboarding.
     * 
     * @param uid User ID
     * @return true if profile exists and has homeCity set, false otherwise
     * 
     * Onboarding is considered complete if:
     * - User profile exists in Firestore
     * - homeCity is not empty (user selected their city)
     * 
     * Why check homeCity?
     * - It's the minimum required field for app to work
     * - Mode detection depends on homeCity
     */
    suspend fun hasCompletedOnboarding(uid: String): Boolean {
        return try {
            val profile = getUserProfile(uid).getOrNull()
            // Profile exists AND homeCity is not empty
            profile != null && profile.homeCity.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get current user's profile.
     * Convenience function that uses current Firebase user's UID.
     * 
     * @return Result with UserProfile on success, Exception on failure
     */
    suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        val uid = firebaseAuth.currentUser?.uid 
            ?: return Result.failure(Exception("No user logged in"))
        
        return getUserProfile(uid)
    }
}