package com.example.androidpractices.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.getSystemService
import com.example.androidpractices.domain.model.ClassTime
import com.example.androidpractices.domain.repository.ClassReminderScheduler

/**
 * Будильник через AlarmManager → [ClassReminderReceiver].
 *
 * Точный будильник нужен, чтобы уведомление пришло ровно к началу пары.
 * На Android 12–12L его даёт SCHEDULE_EXACT_ALARM, с Android 13 — USE_EXACT_ALARM
 * (оба выдаются при установке). Если пользователь всё же отозвал доступ,
 * ставится обычный будильник: система может немного сдвинуть его ради экономии заряда.
 */
class AlarmClassReminderScheduler(
    private val context: Context,
) : ClassReminderScheduler {

    private val alarmManager: AlarmManager = requireNotNull(context.getSystemService())

    override fun schedule(time: ClassTime, ownerName: String): Long {
        val triggerAt = time.nextTriggerMillis(System.currentTimeMillis())
        val intent = Intent(context, ClassReminderReceiver::class.java)
            .putExtra(ClassReminderReceiver.EXTRA_OWNER_NAME, ownerName)
            .putExtra(ClassReminderReceiver.EXTRA_TIME, time.toString())
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (canScheduleExact()) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager, AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent,
            )
        } else {
            AlarmManagerCompat.setAndAllowWhileIdle(
                alarmManager, AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent,
            )
        }
        return triggerAt
    }

    private fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private companion object {
        const val REQUEST_CODE = 7000
    }
}
