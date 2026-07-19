package com.plantsense.ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.model.ScanType

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "IDENTIFICATION" or "DISEASE"
    // Always an absolute local file path; never a bare content:// URI or remote URL
    val imageUrl: String,
    val timestamp: Long,
    val plantName: String? = null,
    val botanicalName: String? = null,
    val confidence: Double? = null,
    val description: String? = null,
    val careLight: String? = null,
    val careWater: String? = null,
    val careTemp: String? = null,
    val diseaseName: String? = null,
    val diseaseSeverity: String? = null,
    val diseaseCause: String? = null,
    val diseaseSymptoms: String? = null,
    val diseaseTreatment: String? = null,
    val diseasePrevention: String? = null
) {
    fun toDomain(): ScanHistoryItem {
        return ScanHistoryItem(
            id = id,
            type = try { ScanType.valueOf(type) } catch (e: Exception) { ScanType.IDENTIFICATION },
            imageUrl = imageUrl,
            timestamp = timestamp,
            plantName = plantName,
            botanicalName = botanicalName,
            confidence = confidence,
            description = description,
            careLight = careLight,
            careWater = careWater,
            careTemp = careTemp,
            diseaseName = diseaseName,
            diseaseSeverity = diseaseSeverity,
            diseaseCause = diseaseCause,
            diseaseSymptoms = diseaseSymptoms,
            diseaseTreatment = diseaseTreatment,
            diseasePrevention = diseasePrevention
        )
    }

    companion object {
        fun fromDomain(item: ScanHistoryItem): ScanHistoryEntity {
            return ScanHistoryEntity(
                id = item.id,
                type = item.type.name,
                imageUrl = item.imageUrl,
                timestamp = item.timestamp,
                plantName = item.plantName,
                botanicalName = item.botanicalName,
                confidence = item.confidence,
                description = item.description,
                careLight = item.careLight,
                careWater = item.careWater,
                careTemp = item.careTemp,
                diseaseName = item.diseaseName,
                diseaseSeverity = item.diseaseSeverity,
                diseaseCause = item.diseaseCause,
                diseaseSymptoms = item.diseaseSymptoms,
                diseaseTreatment = item.diseaseTreatment,
                diseasePrevention = item.diseasePrevention
            )
        }
    }
}
