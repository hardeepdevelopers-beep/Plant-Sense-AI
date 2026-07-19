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

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val scans: List<ScanHistoryItem>) : HomeUiState
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
            val recent = list.take(3)
            if (recent.isEmpty()) HomeUiState.Empty else HomeUiState.Success(recent)
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
