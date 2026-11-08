package com.example.androidpractices.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.androidpractices.domain.model.UserProfile
import com.example.androidpractices.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Профиль хранится в Preferences DataStore: это одна запись из нескольких строк,
 * таблица Room здесь не нужна, а изменения сразу приходят подписчикам через Flow.
 */
class ProfileRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : ProfileRepository {

    override val profile: Flow<UserProfile> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            UserProfile(
                fullName = prefs[KEY_FULL_NAME].orEmpty(),
                position = prefs[KEY_POSITION].orEmpty(),
                avatarUri = prefs[KEY_AVATAR_URI],
                resumeUrl = prefs[KEY_RESUME_URL].orEmpty(),
            )
        }
        .distinctUntilChanged()

    override suspend fun save(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[KEY_FULL_NAME] = profile.fullName
            prefs[KEY_POSITION] = profile.position
            prefs[KEY_RESUME_URL] = profile.resumeUrl
            val avatarUri = profile.avatarUri
            if (avatarUri == null) {
                prefs.remove(KEY_AVATAR_URI)
            } else {
                prefs[KEY_AVATAR_URI] = avatarUri
            }
        }
    }

    companion object {
        const val DATASTORE_NAME = "user_profile"

        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_POSITION = stringPreferencesKey("position")
        private val KEY_AVATAR_URI = stringPreferencesKey("avatar_uri")
        private val KEY_RESUME_URL = stringPreferencesKey("resume_url")
    }
}
