package com.plantsense.ai.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
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
        
        // Read EXIF orientation and rotate accordingly
        val finalBitmap = try {
            val exifInterface = ExifInterface(imageFile.absolutePath)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            var needsRotation = false
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    matrix.postRotate(90f)
                    needsRotation = true
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    matrix.postRotate(180f)
                    needsRotation = true
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    matrix.postRotate(270f)
                    needsRotation = true
                }
            }
            if (needsRotation) {
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                rotated
            } else {
                bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
        
        val outputStream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        if (finalBitmap != bitmap) {
            finalBitmap.recycle()
        }
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
