package com.plantsense.ai.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getApiKey(): Flow<String>
    suspend fun saveApiKey(apiKey: String)
}
