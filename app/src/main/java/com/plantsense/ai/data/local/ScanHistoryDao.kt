package com.plantsense.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getScanHistory(): Flow<List<ScanHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanHistory(entity: ScanHistoryEntity)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScanHistoryItem(id: Int)

    @Query("DELETE FROM scan_history")
    suspend fun clearHistory()
}
