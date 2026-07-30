package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val REQUEST_CODE = 2001
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // Show notification to user
        NotificationHelper.showReadingReminderNotification(context)

        // Reschedule for next day if enabled
        if (ReminderManager.isReminderEnabled(context)) {
            val hour = ReminderManager.getReminderHour(context)
            val minute = ReminderManager.getReminderMinute(context)
            ReminderManager.scheduleDailyAlarm(context, hour, minute)
        }
    }
}
