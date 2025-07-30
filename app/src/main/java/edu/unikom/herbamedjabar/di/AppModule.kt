package edu.unikom.herbamedjabar.di

import android.app.Application
import androidx.room.Room
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.unikom.herbamedjabar.dao.ScanHistoryDao
import edu.unikom.herbamedjabar.db.AppDatabase
import edu.unikom.herbamedjabar.repository.PlantRepository
import edu.unikom.herbamedjabar.repository.PlantRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyAMYG07-BBN21gNqGtfYwRUcuAr6kjzQyA" // JANGAN LUPA GANTI API KEY ANDA
        )
    }

    // --- TAMBAHKAN PROVIDER UNTUK DATABASE DAN DAO ---
    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "herb_app_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideScanHistoryDao(db: AppDatabase): ScanHistoryDao {
        return db.scanHistoryDao()
    }

    @Provides
    @Singleton
    fun providePlantRepository(
        generativeModel: GenerativeModel,
        scanHistoryDao: ScanHistoryDao, // Tambahkan DAO sebagai parameter
        app: Application // Tambahkan Application context untuk menyimpan gambar
    ): PlantRepository {
        // Kita akan memperbarui implementasinya nanti
        return PlantRepositoryImpl(generativeModel, scanHistoryDao, app)
    }
}
