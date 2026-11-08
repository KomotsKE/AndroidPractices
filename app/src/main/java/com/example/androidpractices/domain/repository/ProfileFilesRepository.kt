package com.example.androidpractices.domain.repository

import com.example.androidpractices.domain.model.DownloadedFile

/** Файлы профиля: фото и скачанное резюме. URI передаются строками, домен не зависит от Android. */
interface ProfileFilesRepository {

    /** Создаёт пустой файл для камеры и возвращает его content-URI. */
    suspend fun createCameraTarget(): String

    /** Копирует изображение в память приложения и возвращает URI постоянной копии. */
    suspend fun importAvatar(sourceUri: String): String

    /** Удаляет временный снимок и все фото, кроме [keepUri]. */
    suspend fun deleteUnusedAvatars(keepUri: String?)

    /** @throws com.example.androidpractices.domain.model.BreweryException если скачать не удалось */
    suspend fun downloadResume(url: String): DownloadedFile
}
