package com.example.androidpractices.ui.breweries.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpractices.data.model.BreweryListItem
import com.example.androidpractices.data.model.BreweryType
import com.example.androidpractices.data.repository.BreweryRepository
import com.example.androidpractices.data.repository.FavoritesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BreweryListUiState(
    val query: String = "",
    val selectedType: BreweryType? = null,
    val items: List<BreweryListItem> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    /** Первая страница (или перезагрузка после смены фильтра). */
    val isLoading: Boolean = true,
    /** Следующая страница при прокрутке. */
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val errorMessage: String? = null,
)

class BreweryListViewModel(
    private val repository: BreweryRepository,
    favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val state = MutableStateFlow(BreweryListUiState())

    val uiState: StateFlow<BreweryListUiState> =
        combine(state.asStateFlow(), favoritesRepository.ids) { s, favorites ->
            s.copy(favoriteIds = favorites)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = state.value,
        )

    private var loadedPage = 0
    private var loadJob: Job? = null

    init {
        reload()
    }

    fun onQueryChange(query: String) {
        if (query == state.value.query) return
        state.update { it.copy(query = query) }
        reload(debounceMs = SEARCH_DEBOUNCE_MS)
    }

    fun onTypeSelected(type: BreweryType?) {
        if (type == state.value.selectedType) return
        state.update { it.copy(selectedType = type) }
        reload()
    }

    fun retry() {
        if (state.value.items.isEmpty()) reload() else loadNextPage()
    }

    fun loadNextPage() {
        val current = state.value
        if (current.isLoading || current.isLoadingMore || current.endReached || current.errorMessage != null) return
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoadingMore = true) }
            try {
                val page = loadedPage + 1
                val result = fetch(page)
                loadedPage = page
                state.update {
                    it.copy(
                        items = it.items + result,
                        isLoadingMore = false,
                        endReached = result.size < PAGE_SIZE,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update { it.copy(isLoadingMore = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    private fun reload(debounceMs: Long = 0) {
        loadJob?.cancel()
        state.update {
            it.copy(isLoading = true, isLoadingMore = false, endReached = false, errorMessage = null)
        }
        loadJob = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)
            try {
                val result = fetch(page = 1)
                loadedPage = 1
                state.update {
                    it.copy(items = result, isLoading = false, endReached = result.size < PAGE_SIZE)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update {
                    it.copy(items = emptyList(), isLoading = false, errorMessage = e.toUserMessage())
                }
            }
        }
    }

    private suspend fun fetch(page: Int): List<BreweryListItem> {
        val current = state.value
        return repository.getBreweries(
            page = page,
            perPage = PAGE_SIZE,
            query = current.query.takeIf { it.isNotBlank() },
            type = current.selectedType,
        )
    }

    private fun Exception.toUserMessage(): String =
        message?.takeIf { it.isNotBlank() } ?: "Не удалось загрузить пивоварни"

    companion object {
        const val PAGE_SIZE = 10
        private const val SEARCH_DEBOUNCE_MS = 350L
    }
}
