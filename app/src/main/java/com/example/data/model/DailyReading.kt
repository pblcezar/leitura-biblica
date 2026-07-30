package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_readings",
    foreignKeys = [
        ForeignKey(
            entity = ReadingPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId"])]
)
data class DailyReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val dayNumber: Int,
    val readingSummary: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String? = null
)
