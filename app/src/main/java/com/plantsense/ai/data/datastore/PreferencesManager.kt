package com.plantsense.ai.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.plantsense.ai.BuildConfig

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "plantsense_settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val API_KEY = stringPreferencesKey("gemini_api_key")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { preferences ->
        val savedKey = preferences[API_KEY] ?: ""
        if (savedKey.isBlank()) {
            BuildConfig.G_API_KEY
        } else {
            savedKey
        }
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }
}
