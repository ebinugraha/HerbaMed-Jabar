package edu.unikom.herbamedjabar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val resultText: String,
    val imagePath: String,
    val timestamp: Long = System.currentTimeMillis()
)
