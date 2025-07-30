package edu.unikom.herbamedjabar.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private val scanFragment = ScanFragment()
    private val historyFragment = HistoryFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        setCurrentFragment(scanFragment)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_scan -> setCurrentFragment(scanFragment)
                R.id.navigation_history -> setCurrentFragment(historyFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, fragment)
            commit()
        }
    }
}