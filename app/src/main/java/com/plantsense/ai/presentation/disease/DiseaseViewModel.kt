package com.plantsense.ai.presentation.disease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.core.error.ErrorMapper
import com.plantsense.ai.core.network.NetworkResult
import com.plantsense.ai.domain.model.DiseaseDetectionResult
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.model.ScanType
import com.plantsense.ai.domain.usecase.DetectDiseaseUseCase
import com.plantsense.ai.domain.usecase.GetScanHistoryItemUseCase
import com.plantsense.ai.domain.usecase.SaveScanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DiseaseUiState {
    object Loading : DiseaseUiState
    data class Success(val result: DiseaseDetectionResult) : DiseaseUiState
    data class Error(val message: String, val messageResId: Int? = null) : DiseaseUiState
}

@HiltViewModel
class DiseaseViewModel @Inject constructor(
    private val detectDiseaseUseCase: DetectDiseaseUseCase,
    private val saveScanUseCase: SaveScanUseCase,
    private val getScanHistoryItemUseCase: GetScanHistoryItemUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiseaseUiState>(DiseaseUiState.Loading)
    val uiState: StateFlow<DiseaseUiState> = _uiState

    private var hasSaved = false

    fun loadResult(historyId: Int, imagePath: String) {
        if (historyId != -1) {
            viewModelScope.launch(ioDispatcher) {
                _uiState.value = DiseaseUiState.Loading
                val item = getScanHistoryItemUseCase(historyId)
                if (item != null) {
                    _uiState.value = DiseaseUiState.Success(
                        DiseaseDetectionResult(
                            isHealthy = item.diseaseName.isNullOrEmpty() || item.diseaseName == "Healthy",
                            diseaseName = item.diseaseName,
                            cause = item.diseaseCause,
                            severity = item.diseaseSeverity,
                            symptoms = item.diseaseSymptoms,
                            treatment = item.diseaseTreatment,
                            prevention = item.diseasePrevention
                        )
                    )
                } else {
                    _uiState.value = DiseaseUiState.Error("This scan is no longer available")
                }
            }
        } else {
            diagnose(imagePath)
        }
    }

    fun diagnose(imagePath: String) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = DiseaseUiState.Loading
            when (val result = detectDiseaseUseCase(imagePath)) {
                is NetworkResult.Success -> {
                    _uiState.value = DiseaseUiState.Success(result.data)
                    if (!hasSaved) {
                        hasSaved = true
                        saveScanUseCase(
                            ScanHistoryItem(
                                id = 0,
                                type = ScanType.DISEASE,
                                imageUrl = imagePath,
                                timestamp = System.currentTimeMillis(),
                                plantName = null,
                                botanicalName = null,
                                confidence = null,
                                description = null,
                                careLight = null,
                                careWater = null,
                                careTemp = null,
                                diseaseName = result.data.diseaseName ?: "Healthy",
                                diseaseSeverity = result.data.severity,
                                diseaseCause = result.data.cause,
                                diseaseSymptoms = result.data.symptoms,
                                diseaseTreatment = result.data.treatment,
                                diseasePrevention = result.data.prevention
                            )
                        )
                    }
                }
                is NetworkResult.ApiError -> {
                    val uiError = ErrorMapper.mapToUiError(result)
                    _uiState.value = DiseaseUiState.Error(
                        message = uiError.fallbackMessage ?: "API Error",
                        messageResId = uiError.messageResId
                    )
                }
                is NetworkResult.NetworkError -> {
                    val uiError = ErrorMapper.mapToUiError(result)
                    _uiState.value = DiseaseUiState.Error(
                        message = uiError.fallbackMessage ?: "Network Error",
                        messageResId = uiError.messageResId
                    )
                }
                is NetworkResult.UnknownError -> {
                    val uiError = ErrorMapper.mapToUiError(result)
                    _uiState.value = DiseaseUiState.Error(
                        message = uiError.fallbackMessage ?: "Unknown Error",
                        messageResId = uiError.messageResId
                    )
                }
            }
        }
    }
}
