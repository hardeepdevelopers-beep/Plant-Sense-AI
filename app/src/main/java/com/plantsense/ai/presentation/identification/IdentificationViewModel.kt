package com.plantsense.ai.presentation.identification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.model.ScanType
import com.plantsense.ai.domain.usecase.GetApiKeyUseCase
import com.plantsense.ai.domain.usecase.GetScanHistoryItemUseCase
import com.plantsense.ai.domain.usecase.IdentifyPlantUseCase
import com.plantsense.ai.domain.usecase.SaveScanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface IdentificationUiState {
    object Loading : IdentificationUiState
    data class Success(val result: PlantIdentificationResult) : IdentificationUiState
    data class Error(val message: String) : IdentificationUiState
}

@HiltViewModel
class IdentificationViewModel @Inject constructor(
    private val identifyPlantUseCase: IdentifyPlantUseCase,
    private val getApiKeyUseCase: GetApiKeyUseCase,
    private val saveScanUseCase: SaveScanUseCase,
    private val getScanHistoryItemUseCase: GetScanHistoryItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<IdentificationUiState>(IdentificationUiState.Loading)
    val uiState: StateFlow<IdentificationUiState> = _uiState

    private var hasSaved = false

    fun loadResult(historyId: Int, imagePath: String) {
        if (historyId != -1) {
            viewModelScope.launch {
                _uiState.value = IdentificationUiState.Loading
                val item = getScanHistoryItemUseCase(historyId)
                if (item != null) {
                    _uiState.value = IdentificationUiState.Success(
                        PlantIdentificationResult(
                            plantName = item.plantName ?: "Unknown Plant",
                            botanicalName = item.botanicalName ?: "Unknown",
                            confidence = item.confidence ?: 0.0,
                            description = item.description ?: "No description available",
                            careLight = item.careLight ?: "Unknown sunlight requirements",
                            careWater = item.careWater ?: "Unknown water requirements",
                            careTemp = item.careTemp ?: "Unknown temperature requirements"
                        )
                    )
                } else {
                    _uiState.value = IdentificationUiState.Error("This scan is no longer available")
                }
            }
        } else {
            identify(imagePath)
        }
    }

    fun identify(imagePath: String) {
        viewModelScope.launch {
            _uiState.value = IdentificationUiState.Loading
            try {
                val apiKey = getApiKeyUseCase().first()
                if (apiKey.isBlank()) {
                    _uiState.value = IdentificationUiState.Error("API Key is missing. Please set it in Settings.")
                    return@launch
                }

                val file = File(imagePath)
                if (!file.exists()) {
                    _uiState.value = IdentificationUiState.Error("Captured image not found.")
                    return@launch
                }

                identifyPlantUseCase(apiKey, file)
                    .onSuccess { result ->
                        _uiState.value = IdentificationUiState.Success(result)
                        // Save to history once
                        if (!hasSaved) {
                            hasSaved = true
                            saveScanUseCase(
                                ScanHistoryItem(
                                    id = 0,
                                    type = ScanType.IDENTIFICATION,
                                    imageUrl = imagePath,
                                    timestamp = System.currentTimeMillis(),
                                    plantName = result.plantName,
                                    botanicalName = result.botanicalName,
                                    confidence = result.confidence,
                                    description = result.description,
                                    careLight = result.careLight,
                                    careWater = result.careWater,
                                    careTemp = result.careTemp,
                                    diseaseName = null,
                                    diseaseSeverity = null,
                                    diseaseCause = null,
                                    diseaseSymptoms = null,
                                    diseaseTreatment = null,
                                    diseasePrevention = null
                                )
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = IdentificationUiState.Error(error.localizedMessage ?: "Unknown error occurred")
                    }
            } catch (e: Exception) {
                _uiState.value = IdentificationUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
