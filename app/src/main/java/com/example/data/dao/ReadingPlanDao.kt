package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ReadingPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingPlanDao {
    @Query("SELECT * FROM reading_plans ORDER BY id DESC")
    fun getAllPlans(): Flow<List<ReadingPlan>>

    @Query("SELECT * FROM reading_plans ORDER BY id DESC")
    suspend fun getAllPlansOneShot(): List<ReadingPlan>

    @Query("SELECT * FROM reading_plans WHERE isActive = 1 LIMIT 1")
    fun getActivePlan(): Flow<ReadingPlan?>

    @Query("SELECT * FROM reading_plans WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlanOneShot(): ReadingPlan?

    @Query("SELECT * FROM reading_plans WHERE id = :id LIMIT 1")
    fun getPlanById(id: Long): Flow<ReadingPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: ReadingPlan): Long

    @Update
    suspend fun updatePlan(plan: ReadingPlan)

    @Query("UPDATE reading_plans SET isActive = 0")
    suspend fun clearActivePlans()

    @Transaction
    suspend fun setActivePlan(id: Long) {
        clearActivePlans()
        setActivePlanById(id)
    }

    @Query("UPDATE reading_plans SET isActive = 1 WHERE id = :id")
    suspend fun setActivePlanById(id: Long)

    @Query("DELETE FROM reading_plans WHERE id = :id")
    suspend fun deletePlanById(id: Long)
}
