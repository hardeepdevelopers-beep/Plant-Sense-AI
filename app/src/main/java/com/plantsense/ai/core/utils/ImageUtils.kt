package com.plantsense.ai.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtils {
    fun getBase64Image(imageFile: File, maxDimension: Int = 1024): String {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imageFile.absolutePath, options)
        
        var srcWidth = options.outWidth
        var srcHeight = options.outHeight
        
        var inSampleSize = 1
        while (srcWidth / 2 >= maxDimension || srcHeight / 2 >= maxDimension) {
            srcWidth /= 2
            srcHeight /= 2
            inSampleSize *= 2
        }
        
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, decodeOptions) ?: return ""
        
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
