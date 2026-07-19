package com.plantsense.ai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.usecase.GetScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getScanHistoryUseCase: GetScanHistoryUseCase
) : ViewModel() {

    val recentScans: StateFlow<List<ScanHistoryItem>> = getScanHistoryUseCase()
        .map { list -> list.take(3) } // Keep the 3 most recent scans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
