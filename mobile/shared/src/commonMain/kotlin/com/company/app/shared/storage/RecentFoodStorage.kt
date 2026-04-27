package com.company.app.shared.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class RecentFoodStorage(private val dataStore: DataStore<Preferences>) {

    private val RECENT_FOODS_KEY = stringPreferencesKey("recent_food_ids")
    private val MAX_RECENT = 10

    suspend fun getRecentIds(): List<Long> {
        val raw = dataStore.data.map { it[RECENT_FOODS_KEY] }.firstOrNull()
        return raw?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
    }

    suspend fun addRecent(foodId: Long) {
        dataStore.edit { prefs ->
            val current = prefs[RECENT_FOODS_KEY]
                ?.split(",")?.mapNotNull { it.toLongOrNull() }?.toMutableList()
                ?: mutableListOf()
            current.remove(foodId)
            current.add(0, foodId)
            prefs[RECENT_FOODS_KEY] = current.take(MAX_RECENT).joinToString(",")
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(RECENT_FOODS_KEY) }
    }
}
