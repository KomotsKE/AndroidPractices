package com.example.androidpractices.domain.usecase

import com.example.androidpractices.domain.model.DownloadedFile
import com.example.androidpractices.domain.repository.ProfileFilesRepository

class DownloadResumeUseCase(private val repository: ProfileFilesRepository) {
    suspend operator fun invoke(url: String): DownloadedFile = repository.downloadResume(url)
}
