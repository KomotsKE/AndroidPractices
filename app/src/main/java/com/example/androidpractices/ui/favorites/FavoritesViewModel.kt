package com.example.androidpractices.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpractices.data.model.BreweryListItem
import com.example.androidpractices.data.repository.BreweryRepository
import com.example.androidpractices.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val items: List<BreweryListItem> = emptyList(),
)

class FavoritesViewModel(
    repository: BreweryRepository,
    favoritesRepository: FavoritesRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = favoritesRepository.ids
        .map { ids ->
            // Последние добавленные — сверху.
            FavoritesUiState(isLoading = false, items = repository.getBreweriesByIds(ids.toList().asReversed()))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )
}
