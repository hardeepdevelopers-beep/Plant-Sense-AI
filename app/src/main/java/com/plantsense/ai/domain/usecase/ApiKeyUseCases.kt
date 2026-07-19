package com.plantsense.ai.domain.usecase

import com.plantsense.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetApiKeyUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<String> = repository.getApiKey()
}

class SaveApiKeyUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(apiKey: String) = repository.saveApiKey(apiKey)
}
