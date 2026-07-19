package com.plantsense.ai.domain.usecase

import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.repository.PlantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScanHistoryUseCase @Inject constructor(
    private val repository: PlantRepository
) {
    operator fun invoke(): Flow<List<ScanHistoryItem>> {
        return repository.getScanHistory()
    }
}
