package com.example.androidpractices.data.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Кэш в памяти, общий для экрана списка и экрана фильтров: нужно ли показывать бейдж
 * «фильтры изменены». Долгосрочно не хранится — после перезапуска значение заново
 * вычисляется из сохранённых фильтров.
 *
 * Создаётся один на процесс в [com.example.androidpractices.di.ServiceLocator]
 * и передаётся во ViewModel через конструктор.
 */
class FilterBadgeCache {

    private val _showBadge = MutableStateFlow(false)
    val showBadge: StateFlow<Boolean> = _showBadge.asStateFlow()

    fun update(showBadge: Boolean) {
        _showBadge.value = showBadge
    }
}
