package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {
    // Scheduled Links
    @Query("SELECT * FROM scheduled_links ORDER BY hour ASC, minute ASC")
    fun getAllScheduledLinks(): Flow<List<ScheduledLink>>

    @Query("SELECT * FROM scheduled_links WHERE id = :id")
    suspend fun getScheduledLinkById(id: Int): ScheduledLink?

    @Query("SELECT * FROM scheduled_links WHERE isActive = 1")
    suspend fun getActiveScheduledLinks(): List<ScheduledLink>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledLink(link: ScheduledLink): Long

    @Update
    suspend fun updateScheduledLink(link: ScheduledLink)

    @Delete
    suspend fun deleteScheduledLink(link: ScheduledLink)

    @Query("DELETE FROM scheduled_links WHERE id = :id")
    suspend fun deleteScheduledLinkById(id: Int)

    // History Logs
    @Query("SELECT * FROM history_logs ORDER BY timestamp DESC")
    fun getAllHistoryLogs(): Flow<List<HistoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryLog(log: HistoryLog): Long

    @Query("DELETE FROM history_logs")
    suspend fun clearHistoryLogs()
}
