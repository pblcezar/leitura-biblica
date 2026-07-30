package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BookDao
import com.example.data.dao.DailyReadingDao
import com.example.data.dao.ReadingPlanDao
import com.example.data.model.Book
import com.example.data.model.DailyReading
import com.example.data.model.ReadingPlan
import com.example.engine.ReadingPlanEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.migration.Migration

@Database(
    entities = [Book::class, ReadingPlan::class, DailyReading::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun readingPlanDao(): ReadingPlanDao
    abstract fun dailyReadingDao(): DailyReadingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_readings ADD COLUMN notes TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plano_biblico_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                val bookDao = db.bookDao()
                val planDao = db.readingPlanDao()
                val readingDao = db.dailyReadingDao()

                // 1. Seed Books
                bookDao.insertBooks(InitialBibleData.books)

                // 2. Create Default Demo Reading Plan: "Bíblia em 1 Ano"
                val defaultPlan = ReadingPlan(
                    title = "Bíblia em 1 Ano",
                    scope = "ALL",
                    startDate = System.currentTimeMillis() - (86400000L * 2), // Started 2 days ago
                    totalDays = 365,
                    isCompleted = false,
                    isActive = true
                )

                val planId = planDao.insertPlan(defaultPlan)

                // 3. Generate Daily Readings
                val readings = ReadingPlanEngine.generateDailyReadings(
                    planId = planId,
                    scope = "ALL",
                    totalDays = 365,
                    allBooks = InitialBibleData.books
                )

                readingDao.insertReadings(readings)

                // 4. Mark Day 1 and Day 2 as completed to show initial streak and progress
                readings.firstOrNull { it.dayNumber == 1 }?.let { day1 ->
                    readingDao.setReadingCompleted(day1.id, true, System.currentTimeMillis() - 86400000L)
                }
                readings.firstOrNull { it.dayNumber == 2 }?.let { day2 ->
                    readingDao.setReadingCompleted(day2.id, true, System.currentTimeMillis())
                }
            }
        }
    }
}
