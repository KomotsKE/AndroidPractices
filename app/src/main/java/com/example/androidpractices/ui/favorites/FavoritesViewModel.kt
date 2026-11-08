package com.example.androidpractices.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val items: List<BreweryListItem> = emptyList(),
)

/**
 * Избранное читается из Room, сеть не нужна: экран работает и в режиме полёта.
 * Любое изменение в таблице (добавили/убрали на экране деталей) приходит сюда само.
 */
class FavoritesViewModel(
    favoritesRepository: FavoritesRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = favoritesRepository.favorites
        .map { breweries ->
            FavoritesUiState(isLoading = false, items = breweries.map { it.toListItem() })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )
}
