package com.company.app.shared.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class GuestStorage(private val dataStore: DataStore<Preferences>) {

    private val SCANS_REMAINING = intPreferencesKey("guest_scans_remaining")

    val scansRemaining: Flow<Int> = dataStore.data.map {
        it[SCANS_REMAINING] ?: SCAN_LIMIT
    }

    suspend fun decrementScan() {
        dataStore.edit { prefs ->
            val current = prefs[SCANS_REMAINING] ?: SCAN_LIMIT
            prefs[SCANS_REMAINING] = (current - 1).coerceAtLeast(0)
        }
    }

    suspend fun peekRemaining(): Int =
        dataStore.data.map { it[SCANS_REMAINING] ?: SCAN_LIMIT }.first()

    companion object {
        const val SCAN_LIMIT = 5
    }
}
