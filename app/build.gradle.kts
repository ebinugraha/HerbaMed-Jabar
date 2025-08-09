import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    alias(libs.plugins.hilt.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.unikom.herbamedjabar"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.unikom.herbamedjabar"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Membaca API key dari local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }

        val apiKey = properties.getProperty("apiKey", "")
        // Menyediakan API key sebagai string resource
        resValue("string", "api_key", apiKey)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        // buildConfig tidak lagi diperlukan untuk ini
    }

}

dependencies {

//    cloudinary
    implementation(libs.cloudinary.android)
    implementation(libs.androidx.core.splashscreen)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)

    implementation(libs.lottie)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Gemini API (Generative AI)
    implementation(libs.generativeai)

    // CameraX untuk fungsionalitas kamera yang lebih mudah
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Coroutines untuk menangani proses background (seperti panggilan API)
    implementation(libs.kotlinx.coroutines.android)

    // Coil untuk memuat gambar dengan mudah
    implementation(libs.coil)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Dagger - Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    // Dukungan Room untuk Coroutines
    implementation(libs.androidx.room.ktx)

    // Markdown rendering
    implementation(libs.markdown)
    implementation(libs.circleimageview)
}
// Tambahkan ini di bagian paling bawah file
kapt {
    correctErrorTypes = true
}
