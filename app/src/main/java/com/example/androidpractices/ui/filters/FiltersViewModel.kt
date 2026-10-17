package com.example.androidpractices.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpractices.data.cache.FilterBadgeCache
import com.example.androidpractices.domain.model.BreweryFilters
import com.example.androidpractices.domain.model.BreweryType
import com.example.androidpractices.domain.repository.FiltersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FiltersUiState(
    /** То, что пользователь сейчас редактирует. Сохраняется только по «Готово». */
    val draft: BreweryFilters = BreweryFilters.Default,
    /** Что сохранено сейчас — чтобы понять, есть ли несохранённые изменения. */
    val saved: BreweryFilters = BreweryFilters.Default,
    val isLoaded: Boolean = false,
    val isSaving: Boolean = false,
    /** Сохранено — экран можно закрывать. */
    val isDone: Boolean = false,
) {
    val hasChanges: Boolean get() = draft.normalized() != saved.normalized()
}

private fun BreweryFilters.normalized() = copy(query = query.trim())

class FiltersViewModel(
    private val filtersRepository: FiltersRepository,
    private val badgeCache: FilterBadgeCache,
) : ViewModel() {

    private val state = MutableStateFlow(FiltersUiState())
    val uiState: StateFlow<FiltersUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = filtersRepository.filters.first()
            state.update { FiltersUiState(draft = saved, saved = saved, isLoaded = true) }
        }
    }

    fun onQueryChange(query: String) = editDraft { it.copy(query = query) }

    fun onStateSelected(usState: String?) = editDraft { it.copy(state = usState) }

    fun onTypeSelected(type: BreweryType?) = editDraft { it.copy(type = type) }

    /** Сбрасывает черновик к значениям по умолчанию (сохранится по «Готово»). */
    fun reset() = editDraft { BreweryFilters.Default }

    fun apply() {
        val current = state.value
        if (!current.isLoaded || current.isSaving) return
        state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val filters = current.draft.normalized()
            filtersRepository.save(filters)
            badgeCache.update(showBadge = !filters.isDefault)
            state.update { it.copy(saved = filters, isSaving = false, isDone = true) }
        }
    }

    private fun editDraft(transform: (BreweryFilters) -> BreweryFilters) {
        state.update { it.copy(draft = transform(it.draft)) }
    }
}
