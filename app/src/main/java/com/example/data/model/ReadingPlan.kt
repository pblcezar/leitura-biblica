package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_plans")
data class ReadingPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val scope: String, // "ALL", "OLD", "NEW", "PSALMS_PROVERBS"
    val startDate: Long,
    val totalDays: Int,
    val isCompleted: Boolean = false,
    val isActive: Boolean = true
)
