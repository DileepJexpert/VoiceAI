package com.voiceai.app.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun getAudioFilePath(context: Context, fileName: String): String {
        val dir = ensureDirectoryExists(context, Constants.AUDIO_DIR)
        return File(dir, fileName).absolutePath
    }

    fun getScanFilePath(context: Context, fileName: String): String {
        val dir = ensureDirectoryExists(context, Constants.SCANS_DIR)
        return File(dir, fileName).absolutePath
    }

    fun getPdfFilePath(context: Context, fileName: String): String {
        val dir = ensureDirectoryExists(context, Constants.PDFS_DIR)
        return File(dir, fileName).absolutePath
    }

    fun generateFileName(prefix: String, extension: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "${prefix}_${timestamp}${extension}"
    }

    fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists()) file.length() else 0L
    }

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) file.delete() else false
    }

    fun ensureDirectoryExists(context: Context, dirName: String): File {
        val dir = File(context.filesDir, dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
