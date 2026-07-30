package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BibleReadingWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_COMPLETED = "com.example.widget.ACTION_TOGGLE_COMPLETED"
        const val EXTRA_READING_ID = "extra_reading_id"
        const val EXTRA_TARGET_COMPLETED = "extra_target_completed"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, BibleReadingWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                val provider = BibleReadingWidgetProvider()
                provider.onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val activePlan = db.readingPlanDao().getActivePlanOneShot()

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_bible_reading)

                    // Open main activity when clicking card background
                    val openAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val openAppPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

                    if (activePlan != null) {
                        val readings = db.dailyReadingDao().getReadingsForPlanOneShot(activePlan.id)

                        val todayNumber = ((System.currentTimeMillis() - activePlan.startDate) / (86400000L))
                            .toInt() + 1
                        val sanitizedToday = todayNumber.coerceIn(1, activePlan.totalDays)

                        val todayReading = readings.firstOrNull { it.dayNumber == sanitizedToday }
                            ?: readings.firstOrNull { !it.isCompleted }
                            ?: readings.lastOrNull()

                        if (todayReading != null) {
                            views.setTextViewText(R.id.widget_day_badge, "DIA ${todayReading.dayNumber}")
                            views.setTextViewText(R.id.widget_reading_summary, todayReading.readingSummary)

                            if (todayReading.isCompleted) {
                                views.setTextViewText(R.id.widget_status_text, "Concluído com sucesso! 🎉")
                                views.setTextViewText(R.id.widget_btn_complete, "✓ Concluído")
                                views.setInt(R.id.widget_btn_complete, "setBackgroundColor", Color.parseColor("#4CAF50"))
                            } else {
                                views.setTextViewText(R.id.widget_status_text, "Pendente para hoje")
                                views.setTextViewText(R.id.widget_btn_complete, "✓ Marcar como Concluído")
                                views.setInt(R.id.widget_btn_complete, "setBackgroundColor", Color.parseColor("#2E5B88"))
                            }

                            // Set action toggle pending intent
                            val toggleIntent = Intent(context, BibleReadingWidgetProvider::class.java).apply {
                                action = ACTION_TOGGLE_COMPLETED
                                putExtra(EXTRA_READING_ID, todayReading.id)
                                putExtra(EXTRA_TARGET_COMPLETED, !todayReading.isCompleted)
                            }

                            val togglePendingIntent = PendingIntent.getBroadcast(
                                context,
                                todayReading.id.toInt(),
                                toggleIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            views.setOnClickPendingIntent(R.id.widget_btn_complete, togglePendingIntent)
                        } else {
                            views.setTextViewText(R.id.widget_day_badge, "PLANO")
                            views.setTextViewText(R.id.widget_reading_summary, activePlan.title)
                            views.setTextViewText(R.id.widget_status_text, "Nenhuma leitura pendente")
                            views.setTextViewText(R.id.widget_btn_complete, "Abrir App")
                            views.setOnClickPendingIntent(R.id.widget_btn_complete, openAppPendingIntent)
                        }
                    } else {
                        views.setTextViewText(R.id.widget_day_badge, "INÍCIO")
                        views.setTextViewText(R.id.widget_reading_summary, "Nenhum plano ativo")
                        views.setTextViewText(R.id.widget_status_text, "Toque para criar seu primeiro plano")
                        views.setTextViewText(R.id.widget_btn_complete, "Criar Plano")
                        views.setOnClickPendingIntent(R.id.widget_btn_complete, openAppPendingIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE_COMPLETED) {
            val readingId = intent.getLongExtra(EXTRA_READING_ID, -1L)
            val targetCompleted = intent.getBooleanExtra(EXTRA_TARGET_COMPLETED, false)

            if (readingId != -1L) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val timestamp = if (targetCompleted) System.currentTimeMillis() else null
                        db.dailyReadingDao().setReadingCompleted(readingId, targetCompleted, timestamp)
                        updateAllWidgets(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
