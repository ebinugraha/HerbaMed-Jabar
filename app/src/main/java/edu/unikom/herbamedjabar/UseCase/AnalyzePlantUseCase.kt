package edu.unikom.herbamedjabar.UseCase

import android.graphics.Bitmap
import edu.unikom.herbamedjabar.repository.PlantRepository
import javax.inject.Inject

class AnalyzePlantUseCase @Inject constructor(
    private val plantRepository: PlantRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): Result<String> {
        return try {
            val prompt = """
            Anda adalah ahli botani. Identifikasi tanaman pada gambar ini. Berikan jawaban yang terstruktur, modern, dan mudah dibaca dengan format di bawah ini. Gunakan emoji yang relevan.

            🌿 **NAMA TANAMAN**
               - **Umum:** [Nama umum tanaman]
               - **Ilmiah:** [Nama ilmiah tanaman]
            
            ---
            
            ✅ **STATUS HERBAL**
               - [Jawab "Ya, tanaman ini adalah herbal." atau "Tidak, tanaman ini bukan herbal."]
            
            ---
            
            ⭐ **MANFAAT UTAMA**
               - [Jika herbal, sebutkan 1-2 manfaat utamanya. Jika tidak, tulis "Tidak memiliki manfaat herbal yang signifikan."]
            """.trimIndent()

            val response = plantRepository.analyzePlant(bitmap, prompt)
            val resultText = response.text ?: "Gagal mendapatkan teks dari respons."
            Result.success(resultText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
