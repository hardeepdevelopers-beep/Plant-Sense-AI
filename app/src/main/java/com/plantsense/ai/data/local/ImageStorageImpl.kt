package com.plantsense.ai.data.local

import android.content.Context
import android.net.Uri
import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.domain.repository.ImageStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ImageStorage {

    override suspend fun copyUriToStorage(uriString: String): String? = withContext(ioDispatcher) {
        try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val scansDir = File(context.filesDir, "scans").apply { mkdirs() }
            val destFile = File(scansDir, "picked_image_${System.currentTimeMillis()}.jpg")
            val outputStream = destFile.outputStream()
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
