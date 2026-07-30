package com.example.engine

import com.example.data.model.Book
import com.example.data.model.DailyReading

data class ChapterUnit(
    val bookName: String,
    val chapterNumber: Int
)

data class CustomReadingSelection(
    val bookName: String,
    val startChapter: Int,
    val endChapter: Int
)

object ReadingPlanEngine {

    /**
     * Generates a list of DailyReading entries based on custom user selections.
     */
    fun generateCustomDailyReadings(
        planId: Long,
        selections: List<CustomReadingSelection>,
        totalDays: Int
    ): List<DailyReading> {
        val flatChapters = mutableListOf<ChapterUnit>()

        for (item in selections) {
            val start = item.startChapter.coerceAtLeast(1)
            val end = item.endChapter.coerceAtLeast(start)
            for (ch in start..end) {
                flatChapters.add(ChapterUnit(item.bookName, ch))
            }
        }

        if (flatChapters.isEmpty() || totalDays <= 0) return emptyList()

        val totalChapters = flatChapters.size
        val sanitizedDays = totalDays.coerceAtMost(totalChapters)

        val baseChaptersPerDay = totalChapters / sanitizedDays
        val remainder = totalChapters % sanitizedDays

        val dailyReadings = mutableListOf<DailyReading>()
        var currentIndex = 0

        for (day in 1..sanitizedDays) {
            val countForToday = baseChaptersPerDay + if (day <= remainder) 1 else 0
            val endIndex = (currentIndex + countForToday).coerceAtMost(totalChapters)

            val todayUnits = flatChapters.subList(currentIndex, endIndex)
            val summary = formatReadingSummary(todayUnits)

            dailyReadings.add(
                DailyReading(
                    planId = planId,
                    dayNumber = day,
                    readingSummary = summary,
                    isCompleted = false,
                    completedAt = null
                )
            )

            currentIndex = endIndex
        }

        return dailyReadings
    }

    /**
     * Generates a list of DailyReading entries based on the scope and number of days.
     */
    fun generateDailyReadings(
        planId: Long,
        scope: String,
        totalDays: Int,
        allBooks: List<Book>
    ): List<DailyReading> {
        val targetBooks = filterBooksByScope(allBooks, scope)
        val flatChapters = mutableListOf<ChapterUnit>()

        for (book in targetBooks) {
            for (ch in 1..book.totalChapters) {
                flatChapters.add(ChapterUnit(book.name, ch))
            }
        }

        if (flatChapters.isEmpty() || totalDays <= 0) return emptyList()

        val totalChapters = flatChapters.size
        val sanitizedDays = totalDays.coerceAtMost(totalChapters)

        val baseChaptersPerDay = totalChapters / sanitizedDays
        val remainder = totalChapters % sanitizedDays

        val dailyReadings = mutableListOf<DailyReading>()
        var currentIndex = 0

        for (day in 1..sanitizedDays) {
            val countForToday = baseChaptersPerDay + if (day <= remainder) 1 else 0
            val endIndex = (currentIndex + countForToday).coerceAtMost(totalChapters)

            val todayUnits = flatChapters.subList(currentIndex, endIndex)
            val summary = formatReadingSummary(todayUnits)

            dailyReadings.add(
                DailyReading(
                    planId = planId,
                    dayNumber = day,
                    readingSummary = summary,
                    isCompleted = false,
                    completedAt = null
                )
            )

            currentIndex = endIndex
        }

        return dailyReadings
    }

    fun filterBooksByScope(allBooks: List<Book>, scope: String): List<Book> {
        return when (scope) {
            "OLD" -> allBooks.filter { it.testament == "OLD" }
            "NEW" -> allBooks.filter { it.testament == "NEW" }
            "PSALMS_PROVERBS" -> allBooks.filter { it.name in listOf("Salmos", "Provérbios") }
            else -> allBooks // "ALL"
        }
    }

    private fun formatReadingSummary(units: List<ChapterUnit>): String {
        if (units.isEmpty()) return "Descanso"

        // Group consecutive chapters by book
        val bookGroups = mutableListOf<MutableList<ChapterUnit>>()
        var currentGroup = mutableListOf<ChapterUnit>()

        for (unit in units) {
            if (currentGroup.isEmpty()) {
                currentGroup.add(unit)
            } else {
                if (currentGroup.last().bookName == unit.bookName) {
                    currentGroup.add(unit)
                } else {
                    bookGroups.add(currentGroup)
                    currentGroup = mutableListOf(unit)
                }
            }
        }
        if (currentGroup.isNotEmpty()) {
            bookGroups.add(currentGroup)
        }

        val summaries = bookGroups.map { group ->
            val bookName = group.first().bookName
            val firstChapter = group.first().chapterNumber
            val lastChapter = group.last().chapterNumber

            if (firstChapter == lastChapter) {
                "$bookName $firstChapter"
            } else {
                "$bookName $firstChapter - $lastChapter"
            }
        }

        return summaries.joinToString("; ")
    }
}
