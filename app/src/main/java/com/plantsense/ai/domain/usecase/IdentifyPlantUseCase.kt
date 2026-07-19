package com.plantsense.ai.domain.usecase

import com.plantsense.ai.core.network.NetworkResult
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.repository.PlantRepository
import javax.inject.Inject

class IdentifyPlantUseCase @Inject constructor(
    private val repository: PlantRepository
) {
    suspend operator fun invoke(imagePath: String): NetworkResult<PlantIdentificationResult> {
        return repository.identifyPlant(imagePath)
    }
}
