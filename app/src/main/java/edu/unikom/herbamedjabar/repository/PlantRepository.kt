package edu.unikom.herbamedjabar.repository

import android.app.Application
import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import edu.unikom.herbamedjabar.dao.ScanHistoryDao
import edu.unikom.herbamedjabar.data.ScanHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

data class AnalysisResult(val resultText: String, val imagePath: String)

interface PlantRepository {
    suspend fun analyzePlant(bitmap: Bitmap, prompt: String): AnalysisResult
    fun getAllHistory(): Flow<List<ScanHistory>>
    suspend fun deleteHistory(history: ScanHistory)
}

class PlantRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val scanHistoryDao: ScanHistoryDao,
    private val application: Application
) : PlantRepository {

    override suspend fun analyzePlant(bitmap: Bitmap, prompt: String): AnalysisResult {
        val maxRetries = 3
        var currentRetry = 0
        var delayTime = 2000L

        while (currentRetry < maxRetries) {
            try {
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }
                val response = generativeModel.generateContent(inputContent)

                response.text?.let { resultText ->
                    // Simpan gambar ke file
                    val imagePath = saveBitmapToFile(bitmap)

                    // Simpan ke database (sebagai side-effect)
                    val history = ScanHistory(resultText = resultText, imagePath = imagePath)
                    scanHistoryDao.insertHistory(history)

                    // Kembalikan objek AnalysisResult yang dibutuhkan untuk navigasi
                    return AnalysisResult(resultText = resultText, imagePath = imagePath)

                } ?: throw Exception("Hasil teks dari AI kosong.")

            } catch (e: Exception) {
                currentRetry++
                if (currentRetry >= maxRetries) {
                    throw e
                }
                delay(delayTime)
                delayTime *= 2
            }
        }
        throw IllegalStateException("Gagal menganalisis tanaman setelah beberapa kali percobaan.")
    }

    override fun getAllHistory(): Flow<List<ScanHistory>> {
        return scanHistoryDao.getAllHistory()
    }

    override suspend fun deleteHistory(history: ScanHistory) {
        return scanHistoryDao.deleteHistory(history)
    }

    private suspend fun saveBitmapToFile(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            val wrapper = application.applicationContext
            val directory = wrapper.getDir("images", android.content.Context.MODE_PRIVATE)
            val file = File(directory, "${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            file.absolutePath
        }
    }
}
