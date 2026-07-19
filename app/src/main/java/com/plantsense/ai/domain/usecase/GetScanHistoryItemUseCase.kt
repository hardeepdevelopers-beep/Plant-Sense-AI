package com.plantsense.ai.domain.usecase

import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.repository.PlantRepository
import javax.inject.Inject

class GetScanHistoryItemUseCase @Inject constructor(
    private val repository: PlantRepository
) {
    suspend operator fun invoke(id: Int): ScanHistoryItem? {
        return repository.getScanHistoryItem(id)
    }
}
