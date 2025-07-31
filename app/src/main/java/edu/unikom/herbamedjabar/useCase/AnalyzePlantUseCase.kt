package edu.unikom.herbamedjabar.useCase

import android.graphics.Bitmap
import edu.unikom.herbamedjabar.repository.PlantRepository
import javax.inject.Inject

class AnalyzePlantUseCase @Inject constructor(
    private val plantRepository: PlantRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): Result<String> {
        return try {
            val prompt = """
                Anda adalah seorang ahli botani dan herbalis berpengalaman. Tugas Anda adalah menganalisis gambar tanaman yang diberikan dan memberikan informasi yang akurat, terstruktur, dan mudah dipahami dalam format Markdown.

                **PENTING:**
                - Jika gambar tidak jelas, bukan tanaman, atau tidak dapat diidentifikasi, jawab HANYA dengan: "Maaf, tanaman tidak dapat diidentifikasi. Pastikan gambar jelas dan fokus pada satu jenis tanaman."
                - Jika berhasil diidentifikasi, gunakan format di bawah ini secara KONSISTEN.

                ---

                ## 🌿 [Nama Umum Tanaman]
                *Nama Ilmiah: [Nama Ilmiah Tanaman]*


                ### 📝 Deskripsi
                *Berikan deskripsi yang jelas dan informatif tentang tanaman ini dalam **satu paragraf singkat**. Jelaskan ciri-ciri fisik utamanya (bentuk daun, bunga, batang) dan karakteristik unik lainnya.*

                ---

                ### 🩺 Potensi Manfaat & Kegunaan
                *Sebutkan HANYA nama potensi manfaat dan kegunaan dalam bentuk daftar bernomor. JANGAN berikan deskripsi detail untuk setiap poin.*

                1. [Manfaat 1]
                2. [Manfaat 2]
                3. (Lanjutkan jika ada)

                ---

                ### ⚠️ Peringatan & Efek Samping
                *Sebutkan HANYA nama potensi efek samping atau peringatan dalam bentuk daftar poin. JANGAN berikan deskripsi detail. Jika tidak ada, tulis "Tidak ada peringatan khusus."*

                - [Peringatan 1]
                - [Peringatan 2]

                ---
            """.trimIndent()

            val response = plantRepository.analyzePlant(bitmap, prompt)
            val resultText = response.text ?: "Gagal mendapatkan teks dari respons."
            Result.success(resultText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
