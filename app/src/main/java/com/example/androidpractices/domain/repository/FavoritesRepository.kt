package com.example.androidpractices.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Избранное в памяти процесса. Сохранение на диск — задача следующих практик.
 * Порядок — в порядке добавления (Set в Kotlin — LinkedHashSet).
 */
class FavoritesRepository {

    private val _ids = MutableStateFlow<Set<String>>(emptySet())
    val ids: StateFlow<Set<String>> = _ids.asStateFlow()

    fun isFavorite(id: String): Boolean = id in _ids.value

    fun toggle(id: String) {
        _ids.update { current -> if (id in current) current - id else current + id }
    }
}
