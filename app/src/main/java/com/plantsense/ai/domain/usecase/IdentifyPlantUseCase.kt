package com.plantsense.ai.domain.usecase

import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.repository.PlantRepository
import java.io.File
import javax.inject.Inject

class IdentifyPlantUseCase @Inject constructor(
    private val repository: PlantRepository
) {
    suspend operator fun invoke(apiKey: String, imageFile: File): Result<PlantIdentificationResult> {
        return repository.identifyPlant(apiKey, imageFile)
    }
}
