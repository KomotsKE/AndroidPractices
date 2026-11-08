package com.example.androidpractices.domain.repository

import com.example.androidpractices.domain.model.BreweryFilters
import kotlinx.coroutines.flow.Flow

/** Настройки фильтров списка. Переживают перезапуск приложения. */
interface FiltersRepository {

    val filters: Flow<BreweryFilters>

    suspend fun save(filters: BreweryFilters)
}
