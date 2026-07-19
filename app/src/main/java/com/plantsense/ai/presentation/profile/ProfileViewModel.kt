package com.plantsense.ai.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.domain.repository.PlantRepository
import com.plantsense.ai.domain.usecase.GetApiKeyUseCase
import com.plantsense.ai.domain.usecase.SaveApiKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getApiKeyUseCase: GetApiKeyUseCase,
    private val saveApiKeyUseCase: SaveApiKeyUseCase,
    private val plantRepository: PlantRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val apiKey: StateFlow<String> = getApiKeyUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun saveApiKey(key: String) {
        viewModelScope.launch(ioDispatcher) {
            saveApiKeyUseCase(key)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(ioDispatcher) {
            plantRepository.clearHistory()
        }
    }
}
