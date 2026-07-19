package com.plantsense.ai.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.domain.repository.PlantRepository
import com.plantsense.ai.domain.usecase.GetApiKeyUseCase
import com.plantsense.ai.domain.usecase.SaveApiKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getApiKeyUseCase: GetApiKeyUseCase,
    private val saveApiKeyUseCase: SaveApiKeyUseCase,
    private val plantRepository: PlantRepository
) : ViewModel() {

    val apiKey: StateFlow<String> = getApiKeyUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            saveApiKeyUseCase(key)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            plantRepository.clearHistory()
        }
    }
}
