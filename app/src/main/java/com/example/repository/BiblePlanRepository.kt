package com.example.repository

import com.example.data.dao.BookDao
import com.example.data.dao.DailyReadingDao
import com.example.data.dao.ReadingPlanDao
import com.example.data.database.InitialBibleData
import com.example.data.model.DailyReading
import com.example.data.model.ReadingPlan
import com.example.engine.CustomReadingSelection
import com.example.engine.ReadingPlanEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class BiblePlanRepository(
    private val bookDao: BookDao,
    private val planDao: ReadingPlanDao,
    private val readingDao: DailyReadingDao
) {

    val allPlans: Flow<List<ReadingPlan>> = planDao.getAllPlans()
    val activePlan: Flow<ReadingPlan?> = planDao.getActivePlan()

    fun getPlanById(id: Long): Flow<ReadingPlan?> = planDao.getPlanById(id)

    fun getReadingsForPlan(planId: Long): Flow<List<DailyReading>> =
        readingDao.getReadingsForPlan(planId)

    fun getCompletedCountForPlan(planId: Long): Flow<Int> =
        readingDao.getCompletedCountForPlan(planId)

    suspend fun ensureBooksSeeded() {
        if (bookDao.getBookCount() == 0) {
            bookDao.insertBooks(InitialBibleData.books)
        }
    }

    suspend fun createNewPlan(
        title: String,
        scope: String,
        totalDays: Int,
        startDate: Long
    ): Long {
        ensureBooksSeeded()

        // Deactivate all previous plans
        planDao.clearActivePlans()

        val newPlan = ReadingPlan(
            title = title,
            scope = scope,
            startDate = startDate,
            totalDays = totalDays,
            isCompleted = false,
            isActive = true
        )

        val planId = planDao.insertPlan(newPlan)

        val readings = ReadingPlanEngine.generateDailyReadings(
            planId = planId,
            scope = scope,
            totalDays = totalDays,
            allBooks = InitialBibleData.books
        )

        readingDao.insertReadings(readings)
        return planId
    }

    suspend fun createCustomPlan(
        title: String,
        selections: List<CustomReadingSelection>,
        totalDays: Int,
        startDate: Long
    ): Long {
        ensureBooksSeeded()

        // Deactivate all previous plans
        planDao.clearActivePlans()

        val newPlan = ReadingPlan(
            title = title,
            scope = "CUSTOM",
            startDate = startDate,
            totalDays = totalDays,
            isCompleted = false,
            isActive = true
        )

        val planId = planDao.insertPlan(newPlan)

        val readings = ReadingPlanEngine.generateCustomDailyReadings(
            planId = planId,
            selections = selections,
            totalDays = totalDays
        )

        readingDao.insertReadings(readings)
        return planId
    }

    suspend fun toggleReadingCompletion(readingId: Long, isCompleted: Boolean) {
        val timestamp = if (isCompleted) System.currentTimeMillis() else null
        readingDao.setReadingCompleted(readingId, isCompleted, timestamp)
    }

    suspend fun saveReadingNotes(readingId: Long, notes: String?) {
        readingDao.updateReadingNotes(readingId, notes)
    }

    suspend fun setActivePlan(planId: Long) {
        planDao.setActivePlan(planId)
    }

    suspend fun deletePlan(planId: Long) {
        planDao.deletePlanById(planId)
    }
}
