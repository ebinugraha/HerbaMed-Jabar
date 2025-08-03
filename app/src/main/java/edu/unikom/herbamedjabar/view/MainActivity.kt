package edu.unikom.herbamedjabar.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.R
import edu.unikom.herbamedjabar.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            // Tampilkan fragment awal (ScanFragment)
            setCurrentFragment(ScanFragment(), false)
        }

        binding.navView.setOnItemSelectedListener { item ->
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

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                binding.navView.visibility = View.GONE
            } else {
                binding.navView.visibility = View.VISIBLE
            }
        }
    }

    private fun setCurrentFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction =
            supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment)

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
    }
}
