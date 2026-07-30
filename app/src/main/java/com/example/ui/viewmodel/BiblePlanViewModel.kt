package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.DailyReading
import com.example.data.model.ReadingPlan
import com.example.repository.BiblePlanRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class BiblePlanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BiblePlanRepository
    private val prefs = application.getSharedPreferences("bible_plan_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        when (prefs.getString("theme_mode", AppThemeMode.SYSTEM.name)) {
            AppThemeMode.LIGHT.name -> AppThemeMode.LIGHT
            AppThemeMode.DARK.name -> AppThemeMode.DARK
            else -> AppThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    init {

        val database = AppDatabase.getDatabase(application)
        repository = BiblePlanRepository(
            bookDao = database.bookDao(),
            planDao = database.readingPlanDao(),
            readingDao = database.dailyReadingDao()
        )

        // Ensure database is populated on initial launch
        viewModelScope.launch {
            repository.ensureBooksSeeded()
        }
    }

    val activePlan: StateFlow<ReadingPlan?> = repository.activePlan
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val activePlanReadings: StateFlow<List<DailyReading>> = activePlan
        .flatMapLatest { plan ->
            if (plan != null) repository.getReadingsForPlan(plan.id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activePlanCompletedCount: StateFlow<Int> = activePlan
        .flatMapLatest { plan ->
            if (plan != null) repository.getCompletedCountForPlan(plan.id) else flowOf(0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val allPlans: StateFlow<List<ReadingPlan>> = repository.allPlans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedPlanId = MutableStateFlow<Long?>(null)
    val selectedPlanId: StateFlow<Long?> = _selectedPlanId.asStateFlow()

    val selectedPlanDetails: StateFlow<ReadingPlan?> = _selectedPlanId
        .flatMapLatest { planId ->
            if (planId != null) repository.getPlanById(planId) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val selectedPlanReadings: StateFlow<List<DailyReading>> = _selectedPlanId
        .flatMapLatest { planId ->
            if (planId != null) repository.getReadingsForPlan(planId) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectPlan(planId: Long) {
        _selectedPlanId.value = planId
    }

    fun toggleReadingCompletion(readingId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleReadingCompletion(readingId, isCompleted)
            com.example.widget.BibleReadingWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun saveReadingNotes(readingId: Long, notes: String?) {
        viewModelScope.launch {
            repository.saveReadingNotes(readingId, notes)
        }
    }

    fun createNewPlan(
        title: String,
        scope: String,
        totalDays: Int,
        startDate: Long,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newId = repository.createNewPlan(title, scope, totalDays, startDate)
            _selectedPlanId.value = newId
            com.example.widget.BibleReadingWidgetProvider.updateAllWidgets(getApplication())
            onCreated(newId)
        }
    }

    fun createCustomPlan(
        title: String,
        selections: List<com.example.engine.CustomReadingSelection>,
        totalDays: Int,
        startDate: Long,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newId = repository.createCustomPlan(title, selections, totalDays, startDate)
            _selectedPlanId.value = newId
            com.example.widget.BibleReadingWidgetProvider.updateAllWidgets(getApplication())
            onCreated(newId)
        }
    }

    fun setActivePlan(planId: Long) {
        viewModelScope.launch {
            repository.setActivePlan(planId)
            com.example.widget.BibleReadingWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch {
            repository.deletePlan(planId)
            com.example.widget.BibleReadingWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    /**
     * Calculates current day number in the plan relative to start date.
     */
    fun calculateTodayNumber(startDate: Long, totalDays: Int): Int {
        val now = System.currentTimeMillis()
        val diff = now - startDate
        if (diff < 0) return 1
        val daysDiff = TimeUnit.MILLISECONDS.toDays(diff).toInt() + 1
        return daysDiff.coerceAtMost(totalDays)
    }

    /**
     * Calculates estimated completion date.
     */
    fun calculateEstimatedCompletionDate(startDate: Long, totalDays: Int): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        calendar.add(Calendar.DAY_OF_YEAR, totalDays)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthNames = arrayOf(
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
            "Jul", "Ago", "Set", "Out", "Nov", "Dez"
        )
        val month = monthNames[calendar.get(Calendar.MONTH)]
        val year = calendar.get(Calendar.YEAR)
        return "$day de $month de $year"
    }

    /**
     * Calculates reading streak (consecutive completed readings from start or recent).
     */
    fun calculateStreak(readings: List<DailyReading>): Int {
        if (readings.isEmpty()) return 0
        var streak = 0
        // Find last completed day and count backwards
        val completedDays = readings.filter { it.isCompleted }.map { it.dayNumber }.toSet()
        if (completedDays.isEmpty()) return 0

        val maxCompleted = completedDays.maxOrNull() ?: return 0
        var current = maxCompleted
        while (completedDays.contains(current) && current > 0) {
            streak++
            current--
        }
        return streak
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BiblePlanViewModel::class.java)) {
                return BiblePlanViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
