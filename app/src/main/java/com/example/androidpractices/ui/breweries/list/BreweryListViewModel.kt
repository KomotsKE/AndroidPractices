package com.example.androidpractices.ui.breweries.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpractices.data.cache.FilterBadgeCache
import com.example.androidpractices.domain.model.BreweryFilters
import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.repository.FavoritesRepository
import com.example.androidpractices.domain.repository.FiltersRepository
import com.example.androidpractices.domain.usecase.GetBreweriesUseCase
import com.example.androidpractices.ui.common.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BreweryListUiState(
    /** Сохранённые фильтры, с которыми загружен список. */
    val filters: BreweryFilters = BreweryFilters.Default,
    /** Бейдж на кнопке фильтров — из общего [FilterBadgeCache]. */
    val showFilterBadge: Boolean = false,
    val items: List<BreweryListItem> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    /** Первая страница (или перезагрузка после смены фильтров). */
    val isLoading: Boolean = true,
    /** Следующая страница при прокрутке. */
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val errorMessage: String? = null,
)

class BreweryListViewModel(
    private val getBreweries: GetBreweriesUseCase,
    favoritesRepository: FavoritesRepository,
    private val filtersRepository: FiltersRepository,
    private val badgeCache: FilterBadgeCache,
) : ViewModel() {

    private val state = MutableStateFlow(BreweryListUiState())

    val uiState: StateFlow<BreweryListUiState> =
        combine(
            state.asStateFlow(),
            favoritesRepository.ids,
            badgeCache.showBadge,
        ) { s, favorites, showBadge ->
            s.copy(favoriteIds = favorites, showFilterBadge = showBadge)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = state.value,
        )

    private var loadedPage = 0
    private var loadJob: Job? = null

    init {
        // Первое значение — фильтры из DataStore (переживают перезапуск приложения),
        // каждое следующее — после «Готово» на экране фильтров. На каждое — перезагрузка.
        viewModelScope.launch {
            filtersRepository.filters.collect { filters ->
                // Бейдж долгосрочно не храним: после перезапуска вычисляем его заново.
                badgeCache.update(showBadge = !filters.isDefault)
                state.update { it.copy(filters = filters) }
                reload()
            }
        }
    }

    /** Убрать один фильтр прямо из списка (крестик на чипе). */
    fun clearQuery() = saveFilters { it.copy(query = "") }

    fun clearState() = saveFilters { it.copy(state = null) }

    fun clearType() = saveFilters { it.copy(type = null) }

    fun resetFilters() = saveFilters { BreweryFilters.Default }

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

    private fun saveFilters(transform: (BreweryFilters) -> BreweryFilters) {
        viewModelScope.launch {
            // Сохранение в DataStore само вызовет перезагрузку через подписку в init.
            filtersRepository.save(transform(state.value.filters))
        }
    }

    private fun reload() {
        loadJob?.cancel()
        state.update {
            it.copy(isLoading = true, isLoadingMore = false, endReached = false, errorMessage = null)
        }
        loadJob = viewModelScope.launch {
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

    private suspend fun fetch(page: Int): List<BreweryListItem> =
        getBreweries(page = page, perPage = PAGE_SIZE, filters = state.value.filters)

    companion object {
        const val PAGE_SIZE = 10
    }
}
