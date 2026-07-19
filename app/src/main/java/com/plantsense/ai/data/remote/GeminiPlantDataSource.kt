package com.plantsense.ai.data.remote

import com.google.gson.Gson
import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.core.di.GeminiModel
import com.plantsense.ai.core.network.NetworkResult
import com.plantsense.ai.core.network.safeApiCall
import com.plantsense.ai.core.utils.ImageUtils
import com.plantsense.ai.domain.model.PlantIdentificationResult
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiPlantDataSource @Inject constructor(
    private val apiService: GeminiApiService,
    private val gson: Gson,
    @GeminiModel private val geminiModel: String,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

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

    suspend fun identifyPlant(apiKey: String, imageFile: File): NetworkResult<PlantIdentificationResult> {
        val base64Image = ImageUtils.getBase64Image(imageFile)
        if (base64Image.isEmpty()) {
            return NetworkResult.UnknownError(Exception("Failed to process image"))
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

        val apiResult = safeApiCall(ioDispatcher) {
            apiService.generateContent(geminiModel, apiKey, request)
        }

        return when (apiResult) {
            is NetworkResult.Success -> {
                val response = apiResult.data
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (jsonText == null) {
                    NetworkResult.UnknownError(Exception("Empty response from API"))
                } else {
                    try {
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
                        NetworkResult.Success(result)
                    } catch (e: Exception) {
                        NetworkResult.UnknownError(e)
                    }
                }
            }
            is NetworkResult.ApiError -> apiResult
            is NetworkResult.NetworkError -> apiResult
            is NetworkResult.UnknownError -> apiResult
        }
    }

    private fun cleanJsonString(rawJson: String): String {
        var clean = rawJson.trim()
        if (clean.startsWith("```")) {
            clean = clean.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        }
        return clean
    }
}

private data class RemotePlantIdResult(
    val plantName: String?,
    val botanicalName: String?,
    val confidence: Double?,
    val description: String?,
    val careLight: String?,
    val careWater: String?,
    val careTemp: String?
)
