package com.plantsense.ai.domain.repository

interface ImageStorage {
    suspend fun copyUriToStorage(uriString: String): String?
}
