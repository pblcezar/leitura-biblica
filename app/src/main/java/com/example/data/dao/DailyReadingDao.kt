package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyReading
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReadingDao {
    @Query("SELECT * FROM daily_readings WHERE planId = :planId ORDER BY dayNumber ASC")
    fun getReadingsForPlan(planId: Long): Flow<List<DailyReading>>

    @Query("SELECT * FROM daily_readings WHERE planId = :planId ORDER BY dayNumber ASC")
    suspend fun getReadingsForPlanOneShot(planId: Long): List<DailyReading>

    @Query("SELECT * FROM daily_readings WHERE planId = :planId AND dayNumber = :dayNumber LIMIT 1")
    fun getReadingByDay(planId: Long, dayNumber: Int): Flow<DailyReading?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<DailyReading>)

    @Update
    suspend fun updateReading(reading: DailyReading)

    @Query("UPDATE daily_readings SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setReadingCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE daily_readings SET notes = :notes WHERE id = :id")
    suspend fun updateReadingNotes(id: Long, notes: String?)

    @Query("SELECT COUNT(*) FROM daily_readings WHERE planId = :planId AND isCompleted = 1")
    fun getCompletedCountForPlan(planId: Long): Flow<Int>
}
