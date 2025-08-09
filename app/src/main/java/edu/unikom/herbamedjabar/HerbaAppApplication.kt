package edu.unikom.herbamedjabar

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HerbaAppApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to "difspgu31",
            "api_key" to "152559576226315",
            "api_secret" to "uUY-_zXEUO_UZActj_jPsiRYzIg"
        )
        MediaManager.init(this, config)
    }
}