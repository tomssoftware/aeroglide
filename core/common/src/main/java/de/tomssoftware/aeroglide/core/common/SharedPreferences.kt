package de.tomssoftware.aeroglide.core.common

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun SharedPreferences.getFloatFlow(key: String, defaultValue: Float): Flow<Float> = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, updatedKey ->
        if (updatedKey == key) {
            trySend(sharedPrefs.getFloat(key, defaultValue))
        }
    }
    trySend(getFloat(key, defaultValue))
    registerOnSharedPreferenceChangeListener(listener)
    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}

fun SharedPreferences.getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, updatedKey ->
        if (updatedKey == key) {
            trySend(sharedPrefs.getBoolean(key, defaultValue))
        }
    }
    trySend(getBoolean(key, defaultValue))
    registerOnSharedPreferenceChangeListener(listener)
    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}

