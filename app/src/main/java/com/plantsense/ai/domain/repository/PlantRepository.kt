package com.plantsense.ai.domain.repository

import com.plantsense.ai.domain.model.DiseaseDetectionResult
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.model.ScanHistoryItem
import kotlinx.coroutines.flow.Flow
import java.io.File

interface PlantRepository {
    suspend fun identifyPlant(apiKey: String, imageFile: File): Result<PlantIdentificationResult>
    suspend fun detectDisease(apiKey: String, imageFile: File): Result<DiseaseDetectionResult>
    
    fun getScanHistory(): Flow<List<ScanHistoryItem>>
    suspend fun insertScanHistory(item: ScanHistoryItem)
    suspend fun deleteScanHistoryItem(id: Int)
    suspend fun clearHistory()
}
