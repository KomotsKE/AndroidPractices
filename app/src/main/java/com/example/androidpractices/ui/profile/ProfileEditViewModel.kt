package com.example.androidpractices.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpractices.domain.model.UserProfile
import com.example.androidpractices.domain.repository.ProfileFilesRepository
import com.example.androidpractices.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class ProfileEditUiState(
    /** То, что пользователь сейчас редактирует. Сохраняется только по «Готово». */
    val draft: UserProfile = UserProfile(),
    val isLoaded: Boolean = false,
    val isImportingAvatar: Boolean = false,
    val isSaving: Boolean = false,
    val isResumeUrlInvalid: Boolean = false,
    val errorMessage: String? = null,
    /** Сохранено — экран можно закрывать. */
    val isDone: Boolean = false,
)

class ProfileEditViewModel(
    private val profileRepository: ProfileRepository,
    private val filesRepository: ProfileFilesRepository,
) : ViewModel() {

    private val state = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = profileRepository.profile.first()
            // Если процесс пересоздан во время съёмки, новое фото могло прийти раньше профиля.
            state.update {
                it.copy(
                    draft = saved.copy(avatarUri = it.draft.avatarUri ?: saved.avatarUri),
                    isLoaded = true,
                )
            }
        }
    }

    fun onFullNameChange(value: String) = editDraft { it.copy(fullName = value) }

    fun onPositionChange(value: String) = editDraft { it.copy(position = value) }

    fun onResumeUrlChange(value: String) {
        state.update { it.copy(draft = it.draft.copy(resumeUrl = value), isResumeUrlInvalid = false) }
    }

    /** Файл для камеры; null, если создать его не удалось (ошибка уже показана). */
    suspend fun createCameraTarget(): String? =
        try {
            filesRepository.createCameraTarget()
        } catch (e: IOException) {
            state.update { it.copy(errorMessage = "Не удалось подготовить файл для снимка") }
            null
        }

    fun onAvatarSelected(sourceUri: String) {
        state.update { it.copy(isImportingAvatar = true) }
        viewModelScope.launch {
            try {
                val avatarUri = filesRepository.importAvatar(sourceUri)
                state.update {
                    it.copy(draft = it.draft.copy(avatarUri = avatarUri), isImportingAvatar = false)
                }
            } catch (e: IOException) {
                onAvatarImportFailed()
            } catch (e: SecurityException) {
                onAvatarImportFailed()
            }
        }
    }

    fun save() {
        val current = state.value
        if (!current.isLoaded || current.isSaving || current.isImportingAvatar) return
        val profile = current.draft.normalized()
        if (!UserProfile.isValidResumeUrl(profile.resumeUrl)) {
            state.update { it.copy(isResumeUrlInvalid = true) }
            return
        }
        state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                profileRepository.save(profile)
                filesRepository.deleteUnusedAvatars(keepUri = profile.avatarUri)
                state.update { it.copy(draft = profile, isSaving = false, isDone = true) }
            } catch (e: IOException) {
                state.update { it.copy(isSaving = false, errorMessage = "Не удалось сохранить профиль") }
            }
        }
    }

    fun onErrorShown() = state.update { it.copy(errorMessage = null) }

    private fun onAvatarImportFailed() {
        state.update { it.copy(isImportingAvatar = false, errorMessage = "Не удалось загрузить фото") }
    }

    private fun editDraft(transform: (UserProfile) -> UserProfile) {
        state.update { it.copy(draft = transform(it.draft)) }
    }
}
