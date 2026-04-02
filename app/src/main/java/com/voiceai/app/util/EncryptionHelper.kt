package com.voiceai.app.util

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun encryptFile(inputPath: String, outputPath: String): Boolean {
        return try {
            val inputFile = File(inputPath)
            val outputFile = File(outputPath)

            // EncryptedFile requires the output file to not exist
            if (outputFile.exists()) {
                outputFile.delete()
            }

            val encryptedFile = EncryptedFile.Builder(
                context,
                outputFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            val inputBytes = inputFile.readBytes()
            encryptedFile.openFileOutput().use { outputStream ->
                outputStream.write(inputBytes)
                outputStream.flush()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    fun decryptFile(inputPath: String, outputPath: String): Boolean {
        return try {
            val inputFile = File(inputPath)
            val outputFile = File(outputPath)

            val encryptedFile = EncryptedFile.Builder(
                context,
                inputFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            val decryptedBytes = encryptedFile.openFileInput().use { inputStream ->
                inputStream.readBytes()
            }

            outputFile.writeBytes(decryptedBytes)

            true
        } catch (e: Exception) {
            false
        }
    }
}
