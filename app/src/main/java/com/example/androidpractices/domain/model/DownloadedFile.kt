package com.example.androidpractices.domain.model

/** Скачанный файл, который можно передать стороннему приложению для просмотра. */
data class DownloadedFile(
    val contentUri: String,
    val mimeType: String,
)
