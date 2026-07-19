package com.plantsense.ai.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable data object HomeKey
@Serializable data object CameraKey
@Serializable data class IdentificationResultKey(val imagePath: String, val historyId: Int = -1)
@Serializable data class DiseaseResultKey(val imagePath: String, val historyId: Int = -1)
@Serializable data object HistoryKey
@Serializable data object ProfileKey
