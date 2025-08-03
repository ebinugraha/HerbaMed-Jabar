package edu.unikom.herbamedjabar.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        navView = findViewById(R.id.nav_view)

        if (savedInstanceState == null) {
            // Tampilkan fragment awal (ScanFragment)
            setCurrentFragment(ScanFragment(), false)
        }

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_scan -> {
                    setCurrentFragment(ScanFragment(), false)
                    true
                }
                R.id.navigation_history -> {
                    setCurrentFragment(HistoryFragment(), false)
                    true
                }
                else -> false
            }
        }
    }

    private fun setCurrentFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    // Fungsi publik untuk dipanggil dari ScanFragment
    fun showResultFragment(imagePath: String, resultText: String) {
        val resultFragment = ResultFragment.newInstance(imagePath, resultText)
        // Ganti fragment dan tambahkan ke back stack agar bisa kembali
        setCurrentFragment(resultFragment, true)
        // Sembunyikan bottom navigation di halaman hasil
        navView.visibility = View.GONE
    }

    // Override tombol kembali untuk menampilkan bottom nav lagi
    override fun onBackPressed() {
        super.onBackPressed()
        if (supportFragmentManager.backStackEntryCount == 0) {
            navView.visibility = View.VISIBLE
        }
    }
}
