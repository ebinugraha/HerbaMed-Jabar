package edu.unikom.herbamedjabar.repository

import android.app.Application
import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
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

// Interface sekarang memiliki fungsi untuk mendapatkan riwayat
interface PlantRepository {
    suspend fun analyzePlant(bitmap: Bitmap, prompt: String): GenerateContentResponse
    fun getAllHistory(): Flow<List<ScanHistory>>
}



// Implementasi yang jauh lebih lengkap
class PlantRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val scanHistoryDao: ScanHistoryDao,
    private val application: Application // Dibutuhkan untuk mengakses file system
) : PlantRepository {

    override suspend fun analyzePlant(bitmap: Bitmap, prompt: String): GenerateContentResponse {
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

                // --- LOGIKA BARU: MENYIMPAN HASIL ---
                // Jika respons berhasil dan ada teksnya
                response.text?.let { resultText ->
                    // Simpan gambar ke file dan dapatkan path-nya
                    val imagePath = saveBitmapToFile(bitmap)
                    // Buat objek riwayat
                    val history = ScanHistory(
                        resultText = resultText,
                        imagePath = imagePath
                    )
                    // Simpan ke database menggunakan DAO
                    scanHistoryDao.insertHistory(history)
                }
                // ------------------------------------

                return response // Kembalikan respons asli ke UseCase
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

    private suspend fun saveBitmapToFile(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            val wrapper = application.applicationContext
            // Buat direktori 'images' jika belum ada
            val directory = wrapper.getDir("images", android.content.Context.MODE_PRIVATE)
            // Buat nama file yang unik
            val file = File(directory, "${UUID.randomUUID()}.jpg")

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            // Kembalikan path absolut dari file yang disimpan
            file.absolutePath
        }
    }
}