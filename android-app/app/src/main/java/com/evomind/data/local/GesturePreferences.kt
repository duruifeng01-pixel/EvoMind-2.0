package com.evomind.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.gestureDataStore: DataStore<Preferences> by preferencesDataStore(name = "gesture_settings")

class GesturePreferences(private val context: Context) {

    private val dataStore = context.gestureDataStore

    companion object {
        val SWIPE_ENABLED = booleanPreferencesKey("swipe_enabled")
        val DOUBLE_TAP_ENABLED = booleanPreferencesKey("double_tap_enabled")
        val LONG_PRESS_ENABLED = booleanPreferencesKey("long_press_enabled")
        val GESTURE_HAPTIC = booleanPreferencesKey("gesture_haptic")
    }

    val swipeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SWIPE_ENABLED] ?: true
    }

    val doubleTapEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DOUBLE_TAP_ENABLED] ?: true
    }

    val longPressEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[LONG_PRESS_ENABLED] ?: true
    }

    val gestureHaptic: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[GESTURE_HAPTIC] ?: true
    }

    suspend fun setSwipeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SWIPE_ENABLED] = enabled
        }
    }

    suspend fun setDoubleTapEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DOUBLE_TAP_ENABLED] = enabled
        }
    }

    suspend fun setLongPressEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LONG_PRESS_ENABLED] = enabled
        }
    }

    suspend fun setGestureHaptic(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[GESTURE_HAPTIC] = enabled
        }
    }
}
