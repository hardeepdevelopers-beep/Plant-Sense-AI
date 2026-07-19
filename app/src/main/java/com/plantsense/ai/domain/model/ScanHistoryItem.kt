package com.plantsense.ai.domain.model

data class ScanHistoryItem(
    val id: Int,
    val type: String, // "IDENTIFICATION" or "DISEASE"
    val imageUrl: String,
    val timestamp: Long,
    val plantName: String?,
    val botanicalName: String?,
    val confidence: Double?,
    val description: String?,
    val careLight: String?,
    val careWater: String?,
    val careTemp: String?,
    val diseaseName: String?,
    val diseaseSeverity: String?,
    val diseaseCause: String?,
    val diseaseSymptoms: String?,
    val diseaseTreatment: String?,
    val diseasePrevention: String?
)
