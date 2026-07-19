package com.plantsense.ai.domain.usecase

import com.plantsense.ai.domain.repository.PlantRepository
import javax.inject.Inject

class DeleteScanUseCase @Inject constructor(
    private val repository: PlantRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.deleteScanHistoryItem(id)
    }
}
