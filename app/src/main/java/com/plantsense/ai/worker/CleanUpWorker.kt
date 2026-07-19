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
            timber.log.Timber.d("CleanUpWorker starting cache cleanup.")
            // This worker ONLY cleans context.cacheDir (transient/temp files).
            // It must NEVER be pointed at context.filesDir/scans, which holds
            // permanently saved scan images referenced by Room. Deleting those
            // would silently break thumbnails for saved history entries.
            val cacheDir = applicationContext.cacheDir
            val files = cacheDir.listFiles()
            val timeCutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 24 hours

            var deletedCount = 0
            files?.forEach { file ->
                if (file.name.endsWith(".jpg") && file.lastModified() < timeCutoff) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            timber.log.Timber.d("CleanUpWorker finished. Deleted $deletedCount cache files.")
            Result.success()
        } catch (e: Exception) {
            timber.log.Timber.e(e, "CleanUpWorker failed cache cleanup.")
            Result.failure()
        }
    }
}
