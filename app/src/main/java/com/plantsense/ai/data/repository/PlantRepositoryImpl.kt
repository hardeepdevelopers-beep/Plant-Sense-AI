package com.plantsense.ai.data.repository

import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.core.network.NetworkResult
import com.plantsense.ai.data.local.PlantLocalDataSource
import com.plantsense.ai.data.remote.GeminiDiseaseDataSource
import com.plantsense.ai.data.remote.GeminiPlantDataSource
import com.plantsense.ai.domain.model.DiseaseDetectionResult
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.repository.PlantRepository
import com.plantsense.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantRepositoryImpl @Inject constructor(
    private val plantDataSource: GeminiPlantDataSource,
    private val diseaseDataSource: GeminiDiseaseDataSource,
    private val localDataSource: PlantLocalDataSource,
    private val settingsRepository: SettingsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlantRepository {

    override suspend fun identifyPlant(imagePath: String): NetworkResult<PlantIdentificationResult> =
        withContext(ioDispatcher) {
            val file = File(imagePath)
            if (!file.exists()) {
                return@withContext NetworkResult.UnknownError(FileNotFoundException("Captured image not found."))
            }

            val apiKey = settingsRepository.getApiKey().first()
            if (apiKey.isBlank()) {
                return@withContext NetworkResult.ApiError(401, "API Key is missing. Please set it in Settings.")
            }

            plantDataSource.identifyPlant(apiKey, file)
        }

    override suspend fun detectDisease(imagePath: String): NetworkResult<DiseaseDetectionResult> =
        withContext(ioDispatcher) {
            val file = File(imagePath)
            if (!file.exists()) {
                return@withContext NetworkResult.UnknownError(FileNotFoundException("Captured image not found."))
            }

            val apiKey = settingsRepository.getApiKey().first()
            if (apiKey.isBlank()) {
                return@withContext NetworkResult.ApiError(401, "API Key is missing. Please set it in Settings.")
            }

            diseaseDataSource.detectDisease(apiKey, file)
        }

    override fun getScanHistory(): Flow<List<ScanHistoryItem>> {
        return localDataSource.getScanHistory()
    }

    override suspend fun insertScanHistory(item: ScanHistoryItem) {
        localDataSource.insertScanHistory(item)
    }

    override suspend fun deleteScanHistoryItem(id: Int) {
        localDataSource.deleteScanHistoryItem(id)
    }

    override suspend fun clearHistory() {
        localDataSource.clearHistory()
    }

    override suspend fun getScanHistoryItem(id: Int): ScanHistoryItem? {
        return localDataSource.getScanHistoryItem(id)
    }
}
