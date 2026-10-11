package com.example.androidpractices.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.repository.FavoritesRepository
import com.example.androidpractices.domain.usecase.GetBreweriesByIdsUseCase
import com.example.androidpractices.ui.common.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val items: List<BreweryListItem> = emptyList(),
    val errorMessage: String? = null,
)

class FavoritesViewModel(
    private val getBreweriesByIds: GetBreweriesByIdsUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val state = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = state.asStateFlow()

    private var retryJob: Job? = null

    init {
        // При каждом изменении избранного предыдущая загрузка отменяется (collectLatest).
        viewModelScope.launch {
            favoritesRepository.ids.collectLatest { ids -> load(ids) }
        }
    }

    fun retry() {
        retryJob?.cancel()
        retryJob = viewModelScope.launch { load(favoritesRepository.ids.value) }
    }

    private suspend fun load(ids: Set<String>) {
        // Уже показанный список не прячем под спиннер — он просто обновится.
        state.update { it.copy(isLoading = it.items.isEmpty(), errorMessage = null) }
        try {
            // Последние добавленные — сверху.
            val items = getBreweriesByIds(ids.toList().asReversed())
            state.value = FavoritesUiState(isLoading = false, items = items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            state.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
        }
    }
}
