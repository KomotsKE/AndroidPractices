package com.example.androidpractices.domain.repository

import com.example.androidpractices.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/** Данные профиля. Переживают перезапуск приложения. */
interface ProfileRepository {

    val profile: Flow<UserProfile>

    suspend fun save(profile: UserProfile)
}
