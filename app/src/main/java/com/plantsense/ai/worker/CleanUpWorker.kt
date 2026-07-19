package com.plantsense.ai.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File

class CleanUpWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val cacheDir = applicationContext.cacheDir
            val files = cacheDir.listFiles()
            val timeCutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 24 hours

            files?.forEach { file ->
                if (file.name.endsWith(".jpg") && file.lastModified() < timeCutoff) {
                    file.delete()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
