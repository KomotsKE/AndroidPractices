package com.example.androidpractices.ui.breweries.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpractices.domain.model.Brewery
import com.example.androidpractices.domain.model.BreweryException
import com.example.androidpractices.domain.usecase.GetBreweryDetailsUseCase
import com.example.androidpractices.domain.repository.FavoritesRepository
import com.example.androidpractices.ui.common.toUserMessage
import com.example.androidpractices.ui.navigation.Routes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BreweryDetailUiState {
    data object Loading : BreweryDetailUiState
    data object NotFound : BreweryDetailUiState
    data class Error(val message: String) : BreweryDetailUiState
    data class Content(
        val brewery: Brewery,
        val isFavorite: Boolean,
        /** Сети нет — показана копия из избранного. */
        val isOffline: Boolean = false,
    ) : BreweryDetailUiState
}

private sealed interface LoadResult {
    data object Loading : LoadResult
    data object NotFound : LoadResult
    data class Error(val message: String) : LoadResult
    data class Loaded(val brewery: Brewery, val isOffline: Boolean = false) : LoadResult
}

/**
 * id пивоварни приходит аргументом навигации и попадает в [SavedStateHandle]
 * (переживает и поворот экрана, и смерть процесса).
 *
 * Без интернета экран открывает копию из избранного (Room), если она есть.
 */
class BreweryDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getBreweryDetails: GetBreweryDetailsUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val breweryId: String = checkNotNull(savedStateHandle[Routes.ARG_BREWERY_ID]) {
        "Не передан аргумент ${Routes.ARG_BREWERY_ID}"
    }

    private val loadResult = MutableStateFlow<LoadResult>(LoadResult.Loading)

    val uiState: StateFlow<BreweryDetailUiState> =
        combine(loadResult, favoritesRepository.ids) { result, favorites ->
            when (result) {
                LoadResult.Loading -> BreweryDetailUiState.Loading
                LoadResult.NotFound -> BreweryDetailUiState.NotFound
                is LoadResult.Error -> BreweryDetailUiState.Error(result.message)
                is LoadResult.Loaded -> BreweryDetailUiState.Content(
                    brewery = result.brewery,
                    isFavorite = result.brewery.id in favorites,
                    isOffline = result.isOffline,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BreweryDetailUiState.Loading,
        )

    init {
        load()
    }

    fun retry() = load()

    fun toggleFavorite() {
        val brewery = (loadResult.value as? LoadResult.Loaded)?.brewery ?: return
        viewModelScope.launch { favoritesRepository.toggle(brewery) }
    }

    private fun load() {
        loadResult.value = LoadResult.Loading
        viewModelScope.launch {
            loadResult.value = try {
                val brewery = getBreweryDetails(breweryId)
                if (brewery != null) {
                    // Если пивоварня в избранном — освежаем её локальную копию.
                    favoritesRepository.refresh(brewery)
                    LoadResult.Loaded(brewery)
                } else {
                    LoadResult.NotFound
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Нет сети, но пивоварня в избранном — показываем сохранённую копию.
                val cached = if (e is BreweryException.NoConnection || e is BreweryException.Timeout) {
                    favoritesRepository.get(breweryId)
                } else {
                    null
                }
                cached?.let { LoadResult.Loaded(it, isOffline = true) }
                    ?: LoadResult.Error(e.toUserMessage())
            }
        }
    }
}
