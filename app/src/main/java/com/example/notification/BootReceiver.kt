package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (ReminderManager.isReminderEnabled(context)) {
                val hour = ReminderManager.getReminderHour(context)
                val minute = ReminderManager.getReminderMinute(context)
                ReminderManager.scheduleDailyAlarm(context, hour, minute)
            }
        }
    }
}
