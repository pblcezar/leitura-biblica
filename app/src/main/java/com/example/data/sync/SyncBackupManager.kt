package com.example.data.sync

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.DailyReading
import com.example.data.model.ReadingPlan
import com.example.widget.BibleReadingWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ImportResult(
    val success: Boolean,
    val importedPlansCount: Int,
    val message: String
)

object SyncBackupManager {

    private const val APP_IDENTIFIER = "PlanoBiblico"
    private const val BACKUP_VERSION = 1

    /**
     * Exports all reading plans and their readings to a JSON string.
     * Structured to be 100% compatible with future Firebase Firestore / Realtime DB documents.
     */
    suspend fun exportBackupJson(context: Context): String = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val plans = db.readingPlanDao().getAllPlansOneShot()

        val rootJson = JSONObject()
        rootJson.put("app", APP_IDENTIFIER)
        rootJson.put("version", BACKUP_VERSION)
        rootJson.put("exportedAt", System.currentTimeMillis())
        rootJson.put("firebaseSyncReady", true)

        val plansArray = JSONArray()

        for (plan in plans) {
            val planObj = JSONObject()
            planObj.put("id", plan.id)
            planObj.put("title", plan.title)
            planObj.put("scope", plan.scope)
            planObj.put("startDate", plan.startDate)
            planObj.put("totalDays", plan.totalDays)
            planObj.put("isCompleted", plan.isCompleted)
            planObj.put("isActive", plan.isActive)

            val readings = db.dailyReadingDao().getReadingsForPlanOneShot(plan.id)
            val readingsArray = JSONArray()

            for (reading in readings) {
                val readingObj = JSONObject()
                readingObj.put("dayNumber", reading.dayNumber)
                readingObj.put("readingSummary", reading.readingSummary)
                readingObj.put("isCompleted", reading.isCompleted)
                if (reading.completedAt != null) {
                    readingObj.put("completedAt", reading.completedAt)
                } else {
                    readingObj.put("completedAt", JSONObject.NULL)
                }
                if (!reading.notes.isNullOrBlank()) {
                    readingObj.put("notes", reading.notes)
                } else {
                    readingObj.put("notes", JSONObject.NULL)
                }
                readingsArray.put(readingObj)
            }

            planObj.put("readings", readingsArray)
            plansArray.put(planObj)
        }

        rootJson.put("plans", plansArray)

        return@withContext rootJson.toString(2)
    }

    /**
     * Imports reading plans and progress from a JSON string into local database.
     */
    suspend fun importBackupJson(context: Context, jsonContent: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            val rootJson = JSONObject(jsonContent)

            if (!rootJson.has("plans")) {
                return@withContext ImportResult(
                    success = false,
                    importedPlansCount = 0,
                    message = "Arquivo inválido: estrutura de planos não encontrada."
                )
            }

            val db = AppDatabase.getDatabase(context)
            val plansArray = rootJson.getJSONArray("plans")
            var importedCount = 0

            for (i in 0 until plansArray.length()) {
                val planObj = plansArray.getJSONObject(i)

                val title = planObj.optString("title", "Plano Importado")
                val scope = planObj.optString("scope", "ALL")
                val startDate = planObj.optLong("startDate", System.currentTimeMillis())
                val totalDays = planObj.optInt("totalDays", 365)
                val isCompleted = planObj.optBoolean("isCompleted", false)
                val isActive = planObj.optBoolean("isActive", false)

                if (isActive) {
                    db.readingPlanDao().clearActivePlans()
                }

                val newPlan = ReadingPlan(
                    title = title,
                    scope = scope,
                    startDate = startDate,
                    totalDays = totalDays,
                    isCompleted = isCompleted,
                    isActive = isActive
                )

                val planId = db.readingPlanDao().insertPlan(newPlan)

                if (planObj.has("readings")) {
                    val readingsArray = planObj.getJSONArray("readings")
                    val readingsList = mutableListOf<DailyReading>()

                    for (j in 0 until readingsArray.length()) {
                        val readingObj = readingsArray.getJSONObject(j)
                        val dayNumber = readingObj.optInt("dayNumber", j + 1)
                        val readingSummary = readingObj.optString("readingSummary", "")
                        val readingCompleted = readingObj.optBoolean("isCompleted", false)
                        val completedAt = if (readingObj.isNull("completedAt")) null else readingObj.optLong("completedAt")
                        val notes = if (readingObj.isNull("notes")) null else readingObj.optString("notes")

                        readingsList.add(
                            DailyReading(
                                planId = planId,
                                dayNumber = dayNumber,
                                readingSummary = readingSummary,
                                isCompleted = readingCompleted,
                                completedAt = completedAt,
                                notes = notes
                            )
                        )
                    }

                    db.dailyReadingDao().insertReadings(readingsList)
                }

                importedCount++
            }

            // Refresh app widgets
            BibleReadingWidgetProvider.updateAllWidgets(context)

            return@withContext ImportResult(
                success = true,
                importedPlansCount = importedCount,
                message = "$importedCount plano(s) importado(s) com sucesso!"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ImportResult(
                success = false,
                importedPlansCount = 0,
                message = "Erro ao processar arquivo: ${e.localizedMessage ?: "Formato inválido"}"
            )
        }
    }
}
