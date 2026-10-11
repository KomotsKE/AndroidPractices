package com.example.androidpractices.data.repository

import com.example.androidpractices.data.model.Brewery
import com.example.androidpractices.data.model.BreweryListItem
import com.example.androidpractices.data.model.BreweryType

/**
 * Источник данных о пивоварнях. Методы повторяют запросы Open Brewery DB:
 * - [getBreweries] → `GET /v1/breweries?page=&per_page=&by_name=&by_type=`
 * - [getBrewery]   → `GET /v1/breweries/{id}`
 *
 * Сейчас реализация моковая ([MockBreweryRepository]), в следующей практике
 * появится сетевая — экраны менять не придётся.
 */
interface BreweryRepository {

    /** @param page номер страницы, начиная с 1 (как в API). */
    suspend fun getBreweries(
        page: Int,
        perPage: Int,
        query: String? = null,
        type: BreweryType? = null,
    ): List<BreweryListItem>

    suspend fun getBrewery(id: String): Brewery?

    suspend fun getBreweriesByIds(ids: List<String>): List<BreweryListItem>
}
