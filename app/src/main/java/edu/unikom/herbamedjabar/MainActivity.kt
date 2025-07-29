package edu.unikom.herbamedjabar

import android.Manifest // BARU: Import untuk Manifest
import android.content.pm.PackageManager // BARU: Import untuk PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast // BARU: Import untuk Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat // BARU: Import untuk ContextCompat
import edu.unikom.herbamedjabar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val herbDatabase = setupHerbDatabase()
    private val herbKeys = herbDatabase.keys.toList()

    // Launcher untuk mengambil gambar (sudah ada di kode Anda)
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            processImage(bitmap)
        }
    }

    // BARU: Launcher untuk meminta izin kamera
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Jika pengguna memberikan izin, buka kamera
                Toast.makeText(this, "Izin diberikan", Toast.LENGTH_SHORT).show()
                launchCamera()
            } else {
                // Jika pengguna menolak, berikan pesan
                Toast.makeText(this, "Aplikasi membutuhkan izin kamera untuk memindai tanaman", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // DIMODIFIKASI: Listener klik sekarang memanggil fungsi pengecekan izin
        binding.scanButton.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }
    }

    // BARU: Fungsi untuk memeriksa izin sebelum membuka kamera
    private fun checkCameraPermissionAndLaunch() {
        when {
            // Cek apakah izin sudah diberikan
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Jika sudah, langsung buka kamera
                launchCamera()
            }
            // Jika belum, tampilkan dialog permintaan izin
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        // Fungsi ini tidak berubah, akan dipanggil setelah izin dipastikan
        cameraLauncher.launch(null)
    }

    private fun processImage(bitmap: Bitmap) {
        binding.plantImageView.setImageBitmap(bitmap)
        val randomHerbKey = herbKeys.random()
        val identifiedHerb = herbDatabase[randomHerbKey]
        if (identifiedHerb != null) {
            displayHerbDetails(identifiedHerb)
        }
    }

    private fun displayHerbDetails(herb: Herb) {
        binding.plantNameTextView.text = herb.name
        binding.isHerbalTextView.text = if (herb.isHerbal) "Ya" else "Bukan"
        binding.descriptionTextView.text = herb.description
    }

    data class Herb(
        val name: String,
        val isHerbal: Boolean,
        val description: String
    )

    private fun setupHerbDatabase(): Map<String, Herb> {
        return mapOf(
            "jahe" to Herb(
                name = "Jahe (Zingiber officinale)",
                isHerbal = true,
                description = "Jahe adalah tanaman rimpang yang sangat populer sebagai rempah-rempah dan bahan obat. Rimpangnya berbentuk jemari yang menggembung di ruas-ruas tengah. Rasa dominan pedas disebabkan senyawa keton bernama zingeron. Jahe secara tradisional digunakan untuk meredakan mual, masuk angin, dan sakit tenggorokan."
            ),
            "kunyit" to Herb(
                name = "Kunyit (Curcuma longa)",


                isHerbal = true,
                description = "Kunyit dikenal sebagai bumbu masak dan juga pewarna alami. Senyawa aktif utamanya adalah kurkumin, yang memiliki sifat anti-inflamasi dan antioksidan kuat. Dalam pengobatan herbal, kunyit sering digunakan untuk mengatasi masalah pencernaan, nyeri sendi, dan menjaga kesehatan kulit."
            ),
            "lidah_buaya" to Herb(
                name = "Lidah Buaya (Aloe vera)",
                isHerbal = true,
                description = "Lidah buaya memiliki daun berdaging tebal yang berisi gel bening. Gel ini sangat terkenal untuk pengobatan kulit, seperti menyembuhkan luka bakar ringan, melembapkan kulit, dan mengatasi iritasi. Selain untuk eksternal, jus lidah buaya juga dikonsumsi untuk melancarkan pencernaan."
            ),
            "mawar" to Herb(
                name = "Mawar (Rosa)",
                isHerbal = false,
                description = "Meskipun kelopaknya bisa diolah menjadi teh atau air mawar yang memiliki manfaat, secara umum mawar lebih dikenal sebagai tanaman hias karena keindahan dan keharuman bunganya, bukan sebagai tanaman herbal utama."
            )
        )
    }
}