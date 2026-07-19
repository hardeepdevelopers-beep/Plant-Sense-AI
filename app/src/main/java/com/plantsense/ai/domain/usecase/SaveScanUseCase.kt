package com.plantsense.ai.domain.usecase

import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.repository.PlantRepository
import javax.inject.Inject

class SaveScanUseCase @Inject constructor(
    private val repository: PlantRepository
) {
    suspend operator fun invoke(item: ScanHistoryItem) {
        repository.insertScanHistory(item)
    }
}
