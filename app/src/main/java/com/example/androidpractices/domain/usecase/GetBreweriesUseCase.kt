package com.example.androidpractices.domain.usecase

import com.example.androidpractices.domain.model.BreweryFilters
import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.repository.BreweryRepository

/** Страница списка пивоварен с учётом сохранённых фильтров. */
class GetBreweriesUseCase(private val repository: BreweryRepository) {
    suspend operator fun invoke(
        page: Int,
        perPage: Int,
        filters: BreweryFilters = BreweryFilters.Default,
    ): List<BreweryListItem> = repository.getBreweries(
        page = page,
        perPage = perPage,
        query = filters.query.takeIf { it.isNotBlank() },
        type = filters.type,
        state = filters.state,
    )
}
