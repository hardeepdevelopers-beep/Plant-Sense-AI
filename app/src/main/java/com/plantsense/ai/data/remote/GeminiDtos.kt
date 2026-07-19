package com.plantsense.ai.data.remote

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<Content>,
    @SerializedName("generationConfig") val generationConfig: GenerationConfig? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    @SerializedName("inlineData") val inlineData: InlineData? = null
)

data class InlineData(
    @SerializedName("mimeType") val mimeType: String,
    val data: String // Base64 encoded image string
)

data class GenerationConfig(
    @SerializedName("responseMimeType") val responseMimeType: String = "application/json",
    @SerializedName("responseSchema") val responseSchema: ResponseSchema? = null
)

data class ResponseSchema(
    val type: String,
    val properties: Map<String, SchemaProperty>,
    val required: List<String>? = null
)

data class SchemaProperty(
    val type: String,
    val description: String? = null
)

// Response classes
data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: CandidateContent?
)

data class CandidateContent(
    val parts: List<CandidatePart>?
)

data class CandidatePart(
    val text: String?
)
