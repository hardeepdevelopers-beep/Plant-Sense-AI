package com.plantsense.ai.data.local

import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.domain.model.ScanHistoryItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantLocalDataSource @Inject constructor(
    private val scanHistoryDao: ScanHistoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getScanHistory(): Flow<List<ScanHistoryItem>> {
        return scanHistoryDao.getScanHistory()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    suspend fun insertScanHistory(item: ScanHistoryItem) = withContext(ioDispatcher) {
        scanHistoryDao.insertScanHistory(ScanHistoryEntity.fromDomain(item))
    }

    suspend fun deleteScanHistoryItem(id: Int) = withContext(ioDispatcher) {
        scanHistoryDao.deleteScanHistoryItem(id)
    }

    suspend fun clearHistory() = withContext(ioDispatcher) {
        scanHistoryDao.clearHistory()
    }

    suspend fun getScanHistoryItem(id: Int): ScanHistoryItem? = withContext(ioDispatcher) {
        scanHistoryDao.getScanHistoryItem(id)?.toDomain()
    }
}
