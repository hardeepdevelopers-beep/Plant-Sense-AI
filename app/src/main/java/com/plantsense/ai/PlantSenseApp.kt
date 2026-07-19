package com.plantsense.ai

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.plantsense.ai.worker.CleanUpWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import timber.log.Timber

@HiltAndroidApp
class PlantSenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        scheduleCacheCleanUp()
    }

    private fun scheduleCacheCleanUp() {
        val cleanUpRequest = PeriodicWorkRequestBuilder<CleanUpWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CacheCleanUpWork",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanUpRequest
        )
    }
}
