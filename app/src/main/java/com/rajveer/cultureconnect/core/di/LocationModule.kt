package com.rajveer.cultureconnect.core.di

import android.content.Context
import com.rajveer.cultureconnect.core.location.CityResolver
import com.rajveer.cultureconnect.core.location.LocationManager
import com.rajveer.cultureconnect.core.location.ModeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module to provide Location-related dependencies as Singletons. This ensures we have single
 * instances throughout the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    /**
     * Provides a Singleton instance of LocationManager. This handles fetching the current GPS
     * coordinates.
     */
    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return LocationManager(context)
    }

    /**
     * Provides a Singleton instance of CityResolver. This converts GPS coordinates to city names
     * using Geocoder.
     */
    @Provides
    @Singleton
    fun provideCityResolver(@ApplicationContext context: Context): CityResolver {
        return CityResolver(context)
    }

    /**
     * Provides a Singleton instance of ModeManager. This manages the mode state (local vs
     * traveller).
     */
    @Provides
    @Singleton
    fun provideModeManager(): ModeManager {
        return ModeManager()
    }
}
