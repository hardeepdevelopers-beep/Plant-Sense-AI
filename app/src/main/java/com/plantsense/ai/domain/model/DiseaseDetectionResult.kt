package com.plantsense.ai.domain.model

data class DiseaseDetectionResult(
    val isHealthy: Boolean,
    val diseaseName: String?,
    val cause: String?,
    val severity: String?,
    val symptoms: String?,
    val treatment: String?,
    val prevention: String?
)
