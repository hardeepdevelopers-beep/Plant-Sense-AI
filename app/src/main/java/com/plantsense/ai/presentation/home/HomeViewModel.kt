package com.plantsense.ai.presentation.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.usecase.GetScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val scans: List<ScanHistoryItem>) : HomeUiState
    object Empty : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getScanHistoryUseCase: GetScanHistoryUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getScanHistoryUseCase()
        .map { list ->
            val recent = list.take(3)
            if (recent.isEmpty()) HomeUiState.Empty else HomeUiState.Success(recent)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Loading)

    private val _isCopying = MutableStateFlow(false)
    val isCopying: StateFlow<Boolean> = _isCopying

    fun processPickedImage(uri: Uri, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isCopying.value = true
            try {
                val file = withContext(Dispatchers.IO) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val cacheFile = File(context.cacheDir, "picked_image_${System.currentTimeMillis()}.jpg")
                        val outputStream = cacheFile.outputStream()
                        inputStream?.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        cacheFile
                    } catch (e: Exception) {
                        null
                    }
                }
                file?.let { onResult(it.absolutePath) }
            } finally {
                _isCopying.value = false
            }
        }
    }
}
