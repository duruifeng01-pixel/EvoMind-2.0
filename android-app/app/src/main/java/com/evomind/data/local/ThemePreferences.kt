package com.evomind.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemePreferences(private val context: Context) {

    private val dataStore = context.themeDataStore

    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val PRIMARY_COLOR = stringPreferencesKey("primary_color")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val ANIMATION_ENABLED = booleanPreferencesKey("animation_enabled")
    }

    val darkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: false
    }

    val primaryColor: Flow<String> = dataStore.data.map { preferences ->
        preferences[PRIMARY_COLOR] ?: "blue"
    }

    val fontSize: Flow<String> = dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: "medium"
    }

    val animationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ANIMATION_ENABLED] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    suspend fun setPrimaryColor(color: String) {
        dataStore.edit { preferences ->
            preferences[PRIMARY_COLOR] = color
        }
    }

    suspend fun setFontSize(size: String) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    suspend fun setAnimationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ANIMATION_ENABLED] = enabled
        }
    }
}
