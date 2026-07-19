package com.plantsense.ai.domain.repository

import com.plantsense.ai.core.network.NetworkResult
import com.plantsense.ai.domain.model.DiseaseDetectionResult
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.model.ScanHistoryItem
import kotlinx.coroutines.flow.Flow

interface PlantRepository {
    suspend fun identifyPlant(imagePath: String): NetworkResult<PlantIdentificationResult>
    suspend fun detectDisease(imagePath: String): NetworkResult<DiseaseDetectionResult>
    
    fun getScanHistory(): Flow<List<ScanHistoryItem>>
    suspend fun insertScanHistory(item: ScanHistoryItem)
    suspend fun deleteScanHistoryItem(id: Int)
    suspend fun clearHistory()
    suspend fun getScanHistoryItem(id: Int): ScanHistoryItem?
}
