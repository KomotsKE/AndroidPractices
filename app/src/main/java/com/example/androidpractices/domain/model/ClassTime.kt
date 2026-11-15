package com.example.androidpractices.domain.model

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** Время любимой пары в формате ЧЧ:ММ (24 часа). */
data class ClassTime(val hour: Int, val minute: Int) {

    /** Всегда две цифры на часы и минуты: «09:05». */
    override fun toString(): String = "%02d:%02d".format(Locale.ROOT, hour, minute)

    /**
     * Ближайший момент срабатывания: сегодня, если время ещё не наступило,
     * иначе завтра — в прошлое будильник поставить нельзя.
     */
    fun nextTriggerMillis(nowMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= nowMillis) calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
    }

    companion object {
        private val FORMAT = Regex("""([01]\d|2[0-3]):([0-5]\d)""")

        /** Строго ЧЧ:ММ: «9:30», «24:00» и пустая строка не подходят. */
        fun parse(text: String): ClassTime? {
            val match = FORMAT.matchEntire(text) ?: return null
            val (hour, minute) = match.destructured
            return ClassTime(hour.toInt(), minute.toInt())
        }

        fun isValid(text: String): Boolean = parse(text) != null

        /**
         * Подготавливает ввод с клавиатуры: оставляет не больше четырёх цифр
         * и сам ставит двоеточие после часов, чтобы не искать его на цифровой клавиатуре.
         */
        fun formatInput(raw: String): String {
            val digits = raw.filter(Char::isDigit).take(4)
            return if (digits.length <= 2) digits else digits.take(2) + ":" + digits.drop(2)
        }
    }
}
