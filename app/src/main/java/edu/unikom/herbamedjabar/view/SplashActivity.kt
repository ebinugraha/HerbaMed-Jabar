package edu.unikom.herbamedjabar.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Panggil installSplashScreen() SEBELUM super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Gunakan coroutine untuk menavigasi setelah delay singkat
        lifecycleScope.launchWhenCreated {
            delay(1500) // Tampilkan splash screen selama 1.5 detik

            // Cek status login pengguna
            val destination = if (auth.currentUser != null) {
                MainActivity::class.java
            } else {
                AuthActivity::class.java
            }
            startActivity(Intent(this@SplashActivity, destination))
            finish() // Tutup SplashActivity agar tidak bisa kembali
        }
    }
}
