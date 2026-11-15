package com.example.androidpractices.data.repository

private val UNSAFE_FILE_NAME_CHARS = Regex("[^\\p{L}\\p{N}._-]")

/** Безопасное имя файла из последнего сегмента ссылки; расширение сохраняется. */
internal fun resumeFileName(urlSegment: String): String =
    urlSegment
        .replace(UNSAFE_FILE_NAME_CHARS, "_")
        .trimStart('.')
        .takeLast(100)
        .ifBlank { "resume" }
