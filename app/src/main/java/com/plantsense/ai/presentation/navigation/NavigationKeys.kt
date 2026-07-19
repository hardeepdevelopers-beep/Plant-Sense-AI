package com.plantsense.ai.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object HomeKey : NavKey
@Serializable data object CameraKey : NavKey
@Serializable data class IdentificationResultKey(val imagePath: String) : NavKey
@Serializable data class DiseaseResultKey(val imagePath: String) : NavKey
@Serializable data object HistoryKey : NavKey
@Serializable data object ProfileKey : NavKey
