package com.plantsense.ai.domain.usecase

import com.plantsense.ai.domain.model.DiseaseDetectionResult
import com.plantsense.ai.domain.repository.PlantRepository
import java.io.File
import javax.inject.Inject

class DetectDiseaseUseCase @Inject constructor(
    private val repository: PlantRepository
) {
    suspend operator fun invoke(apiKey: String, imageFile: File): Result<DiseaseDetectionResult> {
        return repository.detectDisease(apiKey, imageFile)
    }
}
