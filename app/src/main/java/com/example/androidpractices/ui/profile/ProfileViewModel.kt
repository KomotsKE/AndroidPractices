package com.example.androidpractices.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpractices.domain.model.BreweryException
import com.example.androidpractices.domain.model.DownloadedFile
import com.example.androidpractices.domain.model.UserProfile
import com.example.androidpractices.domain.repository.ProfileRepository
import com.example.androidpractices.domain.usecase.DownloadResumeUseCase
import com.example.androidpractices.ui.common.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile = UserProfile(),
    val isDownloading: Boolean = false,
    /** Скачанное резюме, которое экран должен открыть. */
    val fileToOpen: DownloadedFile? = null,
    val errorMessage: String? = null,
)

private data class ResumeState(
    val isDownloading: Boolean = false,
    val fileToOpen: DownloadedFile? = null,
    val errorMessage: String? = null,
)

class ProfileViewModel(
    profileRepository: ProfileRepository,
    private val downloadResume: DownloadResumeUseCase,
) : ViewModel() {

    private val resumeState = MutableStateFlow(ResumeState())

    val uiState: StateFlow<ProfileUiState> = combine(
        profileRepository.profile,
        resumeState,
    ) { profile, resume ->
        ProfileUiState(
            isLoading = false,
            profile = profile,
            isDownloading = resume.isDownloading,
            fileToOpen = resume.fileToOpen,
            errorMessage = resume.errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    fun onResumeClick() {
        val url = uiState.value.profile.resumeUrl
        if (url.isBlank() || resumeState.value.isDownloading) return
        resumeState.update { it.copy(isDownloading = true) }
        viewModelScope.launch {
            try {
                val file = downloadResume(url)
                resumeState.update { it.copy(isDownloading = false, fileToOpen = file) }
            } catch (e: BreweryException) {
                resumeState.update { it.copy(isDownloading = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    fun onFileOpened() = resumeState.update { it.copy(fileToOpen = null) }

    fun onErrorShown() = resumeState.update { it.copy(errorMessage = null) }
}
