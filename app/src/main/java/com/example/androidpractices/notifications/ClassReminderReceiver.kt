package com.example.androidpractices.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Получает сигнал AlarmManager в назначенное время и показывает уведомление. */
class ClassReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ClassReminderNotifier.show(
            context = context,
            ownerName = intent.getStringExtra(EXTRA_OWNER_NAME).orEmpty(),
            time = intent.getStringExtra(EXTRA_TIME).orEmpty(),
        )
    }

    companion object {
        const val EXTRA_OWNER_NAME = "owner_name"
        const val EXTRA_TIME = "class_time"
    }
}
