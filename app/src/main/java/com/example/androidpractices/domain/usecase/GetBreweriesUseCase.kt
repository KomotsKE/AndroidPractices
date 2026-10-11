package com.example.androidpractices.domain.usecase

import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.model.BreweryType
import com.example.androidpractices.domain.repository.BreweryRepository

/** Страница списка пивоварен с необязательным поиском по названию и фильтром по типу. */
class GetBreweriesUseCase(private val repository: BreweryRepository) {
    suspend operator fun invoke(
        page: Int,
        perPage: Int,
        query: String? = null,
        type: BreweryType? = null,
    ): List<BreweryListItem> = repository.getBreweries(page, perPage, query, type)
}
