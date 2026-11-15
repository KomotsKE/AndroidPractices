package com.example.androidpractices.domain.repository

import com.example.androidpractices.domain.model.ClassTime

/** Планирует уведомление о начале любимой пары. */
interface ClassReminderScheduler {

    /**
     * Ставит напоминание на ближайшее [time] (сегодня или, если время прошло, завтра)
     * и заменяет предыдущее. [ownerName] — имя из профиля для текста уведомления.
     * Возвращает момент срабатывания в миллисекундах.
     */
    fun schedule(time: ClassTime, ownerName: String): Long
}
