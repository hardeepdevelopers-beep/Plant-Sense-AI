package com.plantsense.ai.domain.model

data class PlantIdentificationResult(
    val plantName: String,
    val botanicalName: String,
    val confidence: Double,
    val description: String,
    val careLight: String,
    val careWater: String,
    val careTemp: String
)
