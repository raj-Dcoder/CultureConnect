package com.rajveer.cultureconnect.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by
        preferencesDataStore(name = "user_preferences")

/** Simple DataStore manager for user preferences Stores login state persistently */
class UserPreferences(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    /** Flow that emits true if user is logged in, false otherwise */
    val isLoggedIn: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[IS_LOGGED_IN] ?: false // Default to false if not set
            }

    /** Save login state */
    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences -> preferences[IS_LOGGED_IN] = isLoggedIn }
    }

    /** Clear all preferences (for logout) */
    suspend fun clear() {
        context.dataStore.edit { preferences -> preferences.clear() }
    }
}
