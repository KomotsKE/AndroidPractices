package com.example.androidpractices

import com.example.androidpractices.domain.model.ClassTime
import com.example.androidpractices.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ClassTimeTest {

    private val zone = TimeZone.getTimeZone("Asia/Yekaterinburg")

    private fun millis(day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(zone).apply {
            clear()
            set(2026, Calendar.NOVEMBER, day, hour, minute, 0)
        }.timeInMillis

    @Test
    fun `parse accepts strict HH mm only`() {
        assertEquals(ClassTime(9, 30), ClassTime.parse("09:30"))
        assertEquals(ClassTime(0, 0), ClassTime.parse("00:00"))
        assertEquals(ClassTime(23, 59), ClassTime.parse("23:59"))
        assertNull(ClassTime.parse(""))
        assertNull(ClassTime.parse("9:30"))
        assertNull(ClassTime.parse("24:00"))
        assertNull(ClassTime.parse("12:60"))
        assertNull(ClassTime.parse("1230"))
        assertNull(ClassTime.parse("12:3"))
    }

    @Test
    fun `toString pads hours and minutes`() {
        assertEquals("08:05", ClassTime(8, 5).toString())
    }

    @Test
    fun `input formatting inserts colon and drops extra characters`() {
        assertEquals("", ClassTime.formatInput(""))
        assertEquals("0", ClassTime.formatInput("0"))
        assertEquals("09", ClassTime.formatInput("09"))
        assertEquals("09:3", ClassTime.formatInput("093"))
        assertEquals("09:30", ClassTime.formatInput("0930"))
        assertEquals("09:30", ClassTime.formatInput("09:30"))
        assertEquals("09:30", ClassTime.formatInput("09:301"))
        assertEquals("12:4", ClassTime.formatInput("1a2:4"))
        // Стёрли последнюю цифру минут — двоеточие исчезает вместе с ней.
        assertEquals("09", ClassTime.formatInput("09:"))
    }

    @Test
    fun `trigger is today when time has not come yet`() {
        val now = millis(day = 15, hour = 8, minute = 0)
        assertEquals(millis(day = 15, hour = 9, minute = 30), ClassTime(9, 30).nextTriggerMillis(now, zone))
    }

    @Test
    fun `trigger moves to tomorrow when time has passed or is now`() {
        val now = millis(day = 15, hour = 9, minute = 30)
        assertEquals(millis(day = 16, hour = 9, minute = 30), ClassTime(9, 30).nextTriggerMillis(now, zone))
        assertEquals(millis(day = 16, hour = 8, minute = 0), ClassTime(8, 0).nextTriggerMillis(now, zone))
    }

    @Test
    fun `class time is kept in profile and trimmed`() {
        assertFalse(UserProfile(favoriteClassTime = "09:30").isEmpty)
        assertEquals("09:30", UserProfile(favoriteClassTime = " 09:30 ").normalized().favoriteClassTime)
        assertTrue(ClassTime.isValid(UserProfile(favoriteClassTime = "09:30").favoriteClassTime))
    }
}
