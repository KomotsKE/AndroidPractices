package com.example.androidpractices.domain.repository

import com.example.androidpractices.domain.model.Brewery
import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.model.BreweryType

/**
 * Источник данных о пивоварнях. Методы повторяют запросы Open Brewery DB:
 * - [getBreweries] → `GET /v1/breweries?page=&per_page=&by_name=&by_type=&by_state=`
 * - [getBrewery]   → `GET /v1/breweries/{id}`
 *
 * Рабочая реализация — [com.example.androidpractices.data.repository.BreweryRepositoryImpl] (Retrofit);
 * [com.example.androidpractices.data.repository.MockBreweryRepository] остаётся для тестов.
 */
interface BreweryRepository {

    /**
     * @param page номер страницы, начиная с 1 (как в API).
     * @param state полное название штата («New York»), `null` — все штаты.
     */
    suspend fun getBreweries(
        page: Int,
        perPage: Int,
        query: String? = null,
        type: BreweryType? = null,
        state: String? = null,
    ): List<BreweryListItem>

    suspend fun getBrewery(id: String): Brewery?

    suspend fun getBreweriesByIds(ids: List<String>): List<BreweryListItem>
}
