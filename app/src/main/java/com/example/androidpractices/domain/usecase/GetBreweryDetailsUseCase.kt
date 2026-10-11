package com.example.androidpractices.domain.usecase

import com.example.androidpractices.domain.model.Brewery
import com.example.androidpractices.domain.repository.BreweryRepository

/** Детали пивоварни по id; `null`, если такой нет. */
class GetBreweryDetailsUseCase(private val repository: BreweryRepository) {
    suspend operator fun invoke(id: String): Brewery? = repository.getBrewery(id)
}
