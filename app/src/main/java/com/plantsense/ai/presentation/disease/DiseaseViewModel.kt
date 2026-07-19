package com.plantsense.ai.presentation.disease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.domain.model.DiseaseDetectionResult
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.usecase.DetectDiseaseUseCase
import com.plantsense.ai.domain.usecase.GetApiKeyUseCase
import com.plantsense.ai.domain.usecase.SaveScanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface DiseaseUiState {
    object Loading : DiseaseUiState
    data class Success(val result: DiseaseDetectionResult) : DiseaseUiState
    data class Error(val message: String) : DiseaseUiState
}

@HiltViewModel
class DiseaseViewModel @Inject constructor(
    private val detectDiseaseUseCase: DetectDiseaseUseCase,
    private val getApiKeyUseCase: GetApiKeyUseCase,
    private val saveScanUseCase: SaveScanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiseaseUiState>(DiseaseUiState.Loading)
    val uiState: StateFlow<DiseaseUiState> = _uiState

    private var hasSaved = false

    fun diagnose(imagePath: String) {
        viewModelScope.launch {
            _uiState.value = DiseaseUiState.Loading
            try {
                val apiKey = getApiKeyUseCase().first()
                if (apiKey.isBlank()) {
                    _uiState.value = DiseaseUiState.Error("API Key is missing. Please set it in Settings.")
                    return@launch
                }

                val file = File(imagePath)
                if (!file.exists()) {
                    _uiState.value = DiseaseUiState.Error("Captured image not found.")
                    return@launch
                }

                detectDiseaseUseCase(apiKey, file)
                    .onSuccess { result ->
                        _uiState.value = DiseaseUiState.Success(result)
                        if (!hasSaved) {
                            hasSaved = true
                            saveScanUseCase(
                                ScanHistoryItem(
                                    id = 0,
                                    type = "DISEASE",
                                    imageUrl = imagePath,
                                    timestamp = System.currentTimeMillis(),
                                    plantName = null,
                                    botanicalName = null,
                                    confidence = null,
                                    description = null,
                                    careLight = null,
                                    careWater = null,
                                    careTemp = null,
                                    diseaseName = result.diseaseName ?: "Healthy",
                                    diseaseSeverity = result.severity,
                                    diseaseCause = result.cause,
                                    diseaseSymptoms = result.symptoms,
                                    diseaseTreatment = result.treatment,
                                    diseasePrevention = result.prevention
                                )
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = DiseaseUiState.Error(error.localizedMessage ?: "Unknown error occurred")
                    }
            } catch (e: Exception) {
                _uiState.value = DiseaseUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
