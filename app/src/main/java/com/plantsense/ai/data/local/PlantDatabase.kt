package com.plantsense.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScanHistoryEntity::class], version = 1, exportSchema = false)
abstract class PlantDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
}
