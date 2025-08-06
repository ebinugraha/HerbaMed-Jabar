package edu.unikom.herbamedjabar.di

import android.app.Application
import androidx.room.Room
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.unikom.herbamedjabar.R // Import R class
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
    fun provideGenerativeModel(app: Application): GenerativeModel {
        // Mengambil API key dari string resource yang dibuat oleh Gradle
        val apiKey = app.getString(R.string.api_key)
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    // --- PROVIDER UNTUK DATABASE DAN DAO TETAP SAMA ---
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
        scanHistoryDao: ScanHistoryDao,
        app: Application
    ): PlantRepository {
        return PlantRepositoryImpl(generativeModel, scanHistoryDao, app)
    }
}
