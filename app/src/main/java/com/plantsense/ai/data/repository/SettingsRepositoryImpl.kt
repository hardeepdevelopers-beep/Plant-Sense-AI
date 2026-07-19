package com.plantsense.ai.data.repository

import com.plantsense.ai.data.datastore.PreferencesManager
import com.plantsense.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : SettingsRepository {
    override fun getApiKey(): Flow<String> = preferencesManager.apiKey
    override suspend fun saveApiKey(apiKey: String) = preferencesManager.saveApiKey(apiKey)
}
