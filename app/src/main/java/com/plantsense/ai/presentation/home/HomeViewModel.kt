package com.plantsense.ai.presentation.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.repository.ImageStorage
import com.plantsense.ai.domain.usecase.GetScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.plantsense.ai.domain.model.ScanType
import java.util.Locale

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val scans: List<ScanHistoryItem>,
        val totalScans: Int,
        val vigorScore: Int,
        val vigorRank: String
    ) : HomeUiState
    object Empty : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getScanHistoryUseCase: GetScanHistoryUseCase,
    private val imageStorage: ImageStorage,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getScanHistoryUseCase()
        .map { list ->
            if (list.isEmpty()) {
                HomeUiState.Empty
            } else {
                val totalScans = list.size
                
                // Calculate average Vigor Score
                val sum = list.sumOf { item ->
                    if (item.type == ScanType.IDENTIFICATION) {
                        (item.confidence ?: 0.8) * 100
                    } else {
                        val isHealthy = item.diseaseName.isNullOrEmpty() || item.diseaseName == "Healthy"
                        if (isHealthy) {
                            100.0
                        } else {
                            when (item.diseaseSeverity?.lowercase(Locale.getDefault())) {
                                "low" -> 70.0
                                "medium" -> 45.0
                                "high" -> 15.0
                                else -> 50.0
                            }
                        }
                    }
                }
                val vigorScore = if (totalScans > 0) (sum / totalScans).toInt() else 0
                
                // Determine Vigor Rank based on scan milestones
                val vigorRank = when {
                    totalScans <= 2 -> "Novice"
                    totalScans <= 5 -> "Explorer"
                    totalScans <= 9 -> "Specialist"
                    else -> "Expert"
                }

                HomeUiState.Success(
                    scans = list.take(3),
                    totalScans = totalScans,
                    vigorScore = vigorScore,
                    vigorRank = vigorRank
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Loading)

    private val _isCopying = MutableStateFlow(false)
    val isCopying: StateFlow<Boolean> = _isCopying

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun processPickedImage(uri: Uri, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isCopying.value = true
            _errorMessage.value = null
            try {
                val path = imageStorage.copyUriToStorage(uri.toString())
                if (path != null) {
                    onResult(path)
                } else {
                    _errorMessage.value = "Failed to process image, please try again."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process image, please try again."
            } finally {
                _isCopying.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
