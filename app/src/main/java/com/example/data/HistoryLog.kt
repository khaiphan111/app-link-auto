package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_logs")
data class HistoryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val linkTitle: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // "SUCCESS", "FAILED"
)
