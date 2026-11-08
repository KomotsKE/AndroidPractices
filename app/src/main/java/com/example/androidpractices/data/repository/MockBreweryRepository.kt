package com.example.androidpractices.data.repository

import com.example.androidpractices.domain.model.Brewery
import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.model.BreweryType
import com.example.androidpractices.domain.repository.BreweryRepository
import com.example.androidpractices.data.remote.BreweryDto
import com.example.androidpractices.data.remote.toDomain
import kotlinx.coroutines.delay

/**
 * Моковая реализация: фильтрует и режет на страницы локальный список так же,
 * как это делает сервер, и имитирует задержку сети.
 */
class MockBreweryRepository(
    source: List<BreweryDto> = MockBreweries.all,
    private val networkDelayMs: Long = 600,
) : BreweryRepository {

    private val breweries: List<Brewery> = source.map { it.toDomain() }

    override suspend fun getBreweries(
        page: Int,
        perPage: Int,
        query: String?,
        type: BreweryType?,
        state: String?,
    ): List<BreweryListItem> {
        require(page >= 1) { "page starts from 1" }
        delay(networkDelayMs)
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()
        return breweries.asSequence()
            .filter { type == null || it.type == type }
            .filter { state == null || it.stateProvince.equals(state, ignoreCase = true) }
            .filter { brewery ->
                normalizedQuery.isEmpty() ||
                    brewery.name.lowercase().contains(normalizedQuery) ||
                    brewery.city.lowercase().contains(normalizedQuery) ||
                    brewery.stateProvince.lowercase().contains(normalizedQuery)
            }
            .drop((page - 1) * perPage)
            .take(perPage)
            .map { it.toListItem() }
            .toList()
    }

    override suspend fun getBrewery(id: String): Brewery? {
        delay(networkDelayMs / 2)
        return breweries.firstOrNull { it.id == id }
    }

    override suspend fun getBreweriesByIds(ids: List<String>): List<BreweryListItem> {
        if (ids.isEmpty()) return emptyList()
        delay(networkDelayMs / 3)
        val byId = breweries.associateBy { it.id }
        return ids.mapNotNull { byId[it]?.toListItem() }
    }
}
