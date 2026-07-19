package com.plantsense.ai.data.repository

import com.google.gson.Gson
import com.plantsense.ai.core.utils.ImageUtils
import com.plantsense.ai.data.local.ScanHistoryDao
import com.plantsense.ai.data.local.ScanHistoryEntity
import com.plantsense.ai.data.remote.Content
import com.plantsense.ai.data.remote.GeminiApiService
import com.plantsense.ai.data.remote.GeminiRequest
import com.plantsense.ai.data.remote.GenerationConfig
import com.plantsense.ai.data.remote.InlineData
import com.plantsense.ai.data.remote.Part
import com.plantsense.ai.data.remote.ResponseSchema
import com.plantsense.ai.data.remote.SchemaProperty
import com.plantsense.ai.domain.model.DiseaseDetectionResult
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.repository.PlantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val scanHistoryDao: ScanHistoryDao
) : PlantRepository {

    private val gson = Gson()

    private val plantIdSchema = ResponseSchema(
        type = "object",
        properties = mapOf(
            "plantName" to SchemaProperty(type = "string", description = "The common name of the plant"),
            "botanicalName" to SchemaProperty(type = "string", description = "The botanical/scientific name of the plant"),
            "confidence" to SchemaProperty(type = "number", description = "Confidence score between 0.0 and 1.0"),
            "description" to SchemaProperty(type = "string", description = "General description of the plant"),
            "careLight" to SchemaProperty(type = "string", description = "Sunlight needs"),
            "careWater" to SchemaProperty(type = "string", description = "Watering needs"),
            "careTemp" to SchemaProperty(type = "string", description = "Temperature needs")
        ),
        required = listOf("plantName", "botanicalName", "confidence", "description", "careLight", "careWater", "careTemp")
    )

    private val diseaseSchema = ResponseSchema(
        type = "object",
        properties = mapOf(
            "isHealthy" to SchemaProperty(type = "boolean", description = "true if the plant is healthy, false otherwise"),
            "diseaseName" to SchemaProperty(type = "string", description = "Name of the disease, null/omitted if healthy"),
            "cause" to SchemaProperty(type = "string", description = "Cause of the disease, null/omitted if healthy"),
            "severity" to SchemaProperty(type = "string", description = "Severity of the disease (Low, Medium, High), null/omitted if healthy"),
            "symptoms" to SchemaProperty(type = "string", description = "Visual signs / symptoms, null/omitted if healthy"),
            "treatment" to SchemaProperty(type = "string", description = "Steps to treat/cure the disease, null/omitted if healthy"),
            "prevention" to SchemaProperty(type = "string", description = "How to prevent the disease, null/omitted if healthy")
        ),
        required = listOf("isHealthy")
    )

    override suspend fun identifyPlant(apiKey: String, imageFile: File): Result<PlantIdentificationResult> {
        return try {
            val base64Image = ImageUtils.getBase64Image(imageFile)
            if (base64Image.isEmpty()) {
                return Result.failure(Exception("Failed to process image"))
            }

            val prompt = """
                Identify the plant in this image. Return a JSON object with: 
                "plantName" (String), 
                "botanicalName" (String), 
                "confidence" (Double between 0.0 and 1.0), 
                "description" (String), 
                "careLight" (String, description of sunlight needs), 
                "careWater" (String, description of watering needs), 
                "careTemp" (String, description of temperature needs). 
                Do not wrap the response in markdown code blocks, return raw JSON.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    responseSchema = plantIdSchema
                )
            )

            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("Empty response from API"))

            val cleanJson = cleanJsonString(jsonText)
            val resultDto = gson.fromJson(cleanJson, RemotePlantIdResult::class.java)
            val result = PlantIdentificationResult(
                plantName = resultDto.plantName ?: "Unknown Plant",
                botanicalName = resultDto.botanicalName ?: "Unknown",
                confidence = resultDto.confidence ?: 0.0,
                description = resultDto.description ?: "No description available",
                careLight = resultDto.careLight ?: "Unknown sunlight requirements",
                careWater = resultDto.careWater ?: "Unknown water requirements",
                careTemp = resultDto.careTemp ?: "Unknown temperature requirements"
            )
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun detectDisease(apiKey: String, imageFile: File): Result<DiseaseDetectionResult> {
        return try {
            val base64Image = ImageUtils.getBase64Image(imageFile)
            if (base64Image.isEmpty()) {
                return Result.failure(Exception("Failed to process image"))
            }

            val prompt = """
                Analyze the plant in this image and diagnose any health issues or diseases. 
                Return a JSON object with: 
                "isHealthy" (Boolean, true if the plant is healthy, false otherwise), 
                "diseaseName" (null or String), 
                "cause" (null or String, what caused the disease), 
                "severity" (null or String: Low, Medium, or High), 
                "symptoms" (null or String, description of visual signs), 
                "treatment" (null or String, steps to fix the issue), 
                "prevention" (null or String, how to prevent it in future).
                If the plant is healthy, set isHealthy to true and all other fields to null.
                Do not wrap the response in markdown code blocks, return raw JSON.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    responseSchema = diseaseSchema
                )
            )

            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("Empty response from API"))

            val cleanJson = cleanJsonString(jsonText)
            val resultDto = gson.fromJson(cleanJson, RemoteDiseaseResult::class.java)
            val result = DiseaseDetectionResult(
                isHealthy = resultDto.isHealthy ?: true,
                diseaseName = resultDto.diseaseName,
                cause = resultDto.cause,
                severity = resultDto.severity,
                symptoms = resultDto.symptoms,
                treatment = resultDto.treatment,
                prevention = resultDto.prevention
            )
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cleanJsonString(rawJson: String): String {
        var clean = rawJson.trim()
        if (clean.startsWith("```")) {
            clean = clean.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        }
        return clean
    }

    override fun getScanHistory(): Flow<List<ScanHistoryItem>> {
        return scanHistoryDao.getScanHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertScanHistory(item: ScanHistoryItem) {
        scanHistoryDao.insertScanHistory(ScanHistoryEntity.fromDomain(item))
    }

    override suspend fun deleteScanHistoryItem(id: Int) {
        scanHistoryDao.deleteScanHistoryItem(id)
    }

    override suspend fun clearHistory() {
        scanHistoryDao.clearHistory()
    }

    override suspend fun getScanHistoryItem(id: Int): ScanHistoryItem? {
        return scanHistoryDao.getScanHistoryItem(id)?.toDomain()
    }
}

// Intermediary parsing models
private data class RemotePlantIdResult(
    val plantName: String?,
    val botanicalName: String?,
    val confidence: Double?,
    val description: String?,
    val careLight: String?,
    val careWater: String?,
    val careTemp: String?
)

private data class RemoteDiseaseResult(
    val isHealthy: Boolean?,
    val diseaseName: String?,
    val cause: String?,
    val severity: String?,
    val symptoms: String?,
    val treatment: String?,
    val prevention: String?
)
