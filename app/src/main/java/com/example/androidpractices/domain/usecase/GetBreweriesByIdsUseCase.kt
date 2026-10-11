package com.example.androidpractices.domain.usecase

import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.repository.BreweryRepository

/** Пивоварни по списку id (для экрана «Избранное»), порядок как в [ids]. */
class GetBreweriesByIdsUseCase(private val repository: BreweryRepository) {
    suspend operator fun invoke(ids: List<String>): List<BreweryListItem> =
        repository.getBreweriesByIds(ids)
}
