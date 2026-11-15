package com.example.androidpractices

import com.example.androidpractices.data.repository.resumeFileName
import com.example.androidpractices.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileTest {

    @Test
    fun `new profile is empty and has no resume`() {
        assertTrue(UserProfile().isEmpty)
        assertFalse(UserProfile().hasResume)
    }

    @Test
    fun `any filled field makes profile non empty`() {
        assertFalse(UserProfile(fullName = "Иван").isEmpty)
        assertFalse(UserProfile(avatarUri = "file:///avatar").isEmpty)
    }

    @Test
    fun `normalized trims text and adds https to bare link`() {
        val profile = UserProfile(
            fullName = "  Иван  ",
            position = " Пивовар ",
            resumeUrl = " example.com/cv.pdf ",
        ).normalized()

        assertEquals("Иван", profile.fullName)
        assertEquals("Пивовар", profile.position)
        assertEquals("https://example.com/cv.pdf", profile.resumeUrl)
    }

    @Test
    fun `normalized keeps blank link empty and explicit scheme unchanged`() {
        assertEquals("", UserProfile(resumeUrl = "   ").normalized().resumeUrl)
        assertEquals("http://a.ru/x", UserProfile(resumeUrl = "http://a.ru/x").normalized().resumeUrl)
    }

    @Test
    fun `resume url validation accepts empty and https only`() {
        assertTrue(UserProfile.isValidResumeUrl(""))
        assertTrue(UserProfile.isValidResumeUrl("https://example.com/cv.pdf"))
        assertFalse(UserProfile.isValidResumeUrl("http://example.com/cv.pdf"))
        assertFalse(UserProfile.isValidResumeUrl("https://"))
        assertFalse(UserProfile.isValidResumeUrl("https://exa mple.com/cv.pdf"))
    }

    @Test
    fun `file name keeps extension and drops unsafe characters`() {
        assertEquals("cv.pdf", resumeFileName("cv.pdf"))
        assertEquals("my_cv.pdf", resumeFileName("my cv.pdf"))
        assertEquals("_etc_passwd", resumeFileName("../etc/passwd"))
        assertEquals("hidden", resumeFileName(".hidden"))
    }

    @Test
    fun `file name falls back when segment is empty and is limited in length`() {
        assertEquals("resume", resumeFileName(""))
        val longName = "a".repeat(200) + ".pdf"
        assertEquals(100, resumeFileName(longName).length)
        assertTrue(resumeFileName(longName).endsWith(".pdf"))
    }
}
