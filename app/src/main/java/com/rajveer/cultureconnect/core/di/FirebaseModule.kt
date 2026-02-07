package com.rajveer.cultureconnect.core.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rajveer.cultureconnect.core.data.AuthRepository
import com.rajveer.cultureconnect.core.data.EventRepository
import com.rajveer.cultureconnect.core.data.HighlightRepository
import com.rajveer.cultureconnect.core.data.SavedEventsRepository
import com.rajveer.cultureconnect.core.data.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Hilt Module to provide Firebase instances as Singletons
@Module
@InstallIn(
        SingletonComponent::class
) // This makes dependencies available throughout the app lifecycle
object FirebaseModule {

    /** Provides a Singleton instance of FirebaseAuth for dependency injection. */
    @Provides @Singleton fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Provides a Singleton instance of FirebaseFirestore for dependency injection. Includes a
     * safety check to ensure Firebase is initialized.
     */
    @Provides
    @Singleton
    fun provideFirestore(@ApplicationContext context: Context): FirebaseFirestore {
        // Double-check initialization if needed
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideHighlightRepository(db: FirebaseFirestore): HighlightRepository =
            HighlightRepository(db)

    @Provides @Singleton fun provideEventRepository(db: FirebaseFirestore) = EventRepository(db)

    @Provides
    @Singleton
    fun provideSavedEventsRepository(auth: FirebaseAuth, db: FirebaseFirestore) =
            SavedEventsRepository(auth, db)

    // ✨ NEW: Provide AuthRepository
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth
    ): AuthRepository = AuthRepository(auth)

    // ✨ NEW: Provide UserRepository
    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): UserRepository = UserRepository(firestore, auth)
}
