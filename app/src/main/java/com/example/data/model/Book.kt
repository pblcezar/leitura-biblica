package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: Int,
    val name: String,
    val testament: String, // "OLD" ou "NEW"
    val totalChapters: Int
)
