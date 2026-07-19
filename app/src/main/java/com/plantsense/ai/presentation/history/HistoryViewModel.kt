package com.plantsense.ai.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.usecase.DeleteScanUseCase
import com.plantsense.ai.domain.usecase.GetScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getScanHistoryUseCase: GetScanHistoryUseCase,
    private val deleteScanUseCase: DeleteScanUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val scanHistory: StateFlow<List<ScanHistoryItem>> = getScanHistoryUseCase()
        .combine(_searchQuery) { history, query ->
            if (query.isBlank()) {
                history
            } else {
                history.filter { item ->
                    val plantMatch = item.plantName?.contains(query, ignoreCase = true) == true
                    val diseaseMatch = item.diseaseName?.contains(query, ignoreCase = true) == true
                    plantMatch || diseaseMatch
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            deleteScanUseCase(id)
        }
    }
}
