package com.plantsense.ai.data.remote

import com.google.gson.Gson
import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.core.di.GeminiModel
import com.plantsense.ai.core.network.NetworkResult
import com.plantsense.ai.core.network.safeApiCall
import com.plantsense.ai.core.utils.ImageUtils
import com.plantsense.ai.domain.model.DiseaseDetectionResult
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiDiseaseDataSource @Inject constructor(
    private val apiService: GeminiApiService,
    private val gson: Gson,
    @GeminiModel private val geminiModel: String,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

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

    suspend fun detectDisease(apiKey: String, imageFile: File): NetworkResult<DiseaseDetectionResult> {
        val base64Image = ImageUtils.getBase64Image(imageFile)
        if (base64Image.isEmpty()) {
            return NetworkResult.UnknownError(Exception("Failed to process image"))
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

private data class RemoteDiseaseResult(
    val isHealthy: Boolean?,
    val diseaseName: String?,
    val cause: String?,
    val severity: String?,
    val symptoms: String?,
    val treatment: String?,
    val prevention: String?
)
