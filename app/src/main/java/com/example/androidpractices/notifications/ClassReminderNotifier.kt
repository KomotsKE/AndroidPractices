package com.example.androidpractices.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.androidpractices.MainActivity
import com.example.androidpractices.R

/** Канал и само уведомление «пара начинается». */
object ClassReminderNotifier {

    private const val CHANNEL_ID = "favorite_class"
    private const val NOTIFICATION_ID = 7001
    private const val OPEN_APP_REQUEST_CODE = 7002

    /** Вызывается при старте приложения; повторное создание канала ничего не меняет. */
    fun createChannel(context: Context) {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName("Любимая пара")
            .setDescription("Напоминание о начале любимой пары из профиля")
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    /** Можно ли сейчас показать уведомление: есть разрешение и пользователь не выключил их. */
    fun canNotify(context: Context): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    @SuppressLint("MissingPermission") // проверено в canNotify
    fun show(context: Context, ownerName: String, time: String) {
        if (!canNotify(context)) return

        val greeting = ownerName.ifBlank { null }?.let { "$it, ваша" } ?: "Ваша"
        val text = if (time.isBlank()) {
            "$greeting любимая пара начинается"
        } else {
            "$greeting любимая пара начинается в $time"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_class)
            .setContentTitle("Пара начинается")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /**
     * Нажатие открывает приложение так же, как иконка на рабочем столе:
     * если оно уже запущено, задача просто выходит на передний план.
     */
    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
