package com.example.androidpractices.domain.model

import java.net.URI
import java.net.URISyntaxException

/** Профиль пользователя. Пустые строки и null означают, что поле не заполнено. */
data class UserProfile(
    val fullName: String = "",
    val position: String = "",
    /** URI изображения на устройстве. */
    val avatarUri: String? = null,
    /** Ссылка на файл резюме или портфолио, например PDF. */
    val resumeUrl: String = "",
) {
    val isEmpty: Boolean
        get() = fullName.isBlank() && position.isBlank() && avatarUri == null && resumeUrl.isBlank()

    val hasResume: Boolean get() = resumeUrl.isNotBlank()

    /** Убирает лишние пробелы и дополняет ссылку без схемы до https. */
    fun normalized(): UserProfile = copy(
        fullName = fullName.trim(),
        position = position.trim(),
        resumeUrl = normalizeResumeUrl(resumeUrl),
    )

    companion object {
        private fun normalizeResumeUrl(url: String): String {
            val trimmed = url.trim()
            return when {
                trimmed.isEmpty() -> ""
                "://" in trimmed -> trimmed
                else -> "https://$trimmed"
            }
        }

        /**
         * Пустая ссылка допустима: поле необязательное. Разрешён только https,
         * потому что незашифрованный трафик Android по умолчанию блокирует.
         */
        fun isValidResumeUrl(url: String): Boolean {
            if (url.isEmpty()) return true
            return try {
                val uri = URI(url)
                uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
            } catch (e: URISyntaxException) {
                false
            }
        }
    }
}
