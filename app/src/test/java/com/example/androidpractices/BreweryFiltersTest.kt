package com.example.androidpractices

import com.example.androidpractices.data.cache.FilterBadgeCache
import com.example.androidpractices.data.repository.MockBreweryRepository
import com.example.androidpractices.domain.model.BreweryFilters
import com.example.androidpractices.domain.model.BreweryType
import com.example.androidpractices.domain.usecase.GetBreweriesUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BreweryFiltersTest {

    private val getBreweries = GetBreweriesUseCase(MockBreweryRepository(networkDelayMs = 0))

    @Test
    fun defaultFilters_areDefault() {
        assertTrue(BreweryFilters.Default.isDefault)
        assertTrue(BreweryFilters(query = "   ").isDefault)
        assertEquals(0, BreweryFilters.Default.activeCount)
    }

    @Test
    fun anyFilter_makesFiltersNonDefault() {
        val filters = BreweryFilters(query = "stone", state = "California", type = BreweryType.MICRO)
        assertFalse(filters.isDefault)
        assertEquals(3, filters.activeCount)
        assertFalse(BreweryFilters(state = "Ohio").isDefault)
        assertFalse(BreweryFilters(type = BreweryType.BAR).isDefault)
    }

    @Test
    fun useCase_appliesAllFilters() = runBlocking {
        val oregonBrewpubs = getBreweries(
            page = 1,
            perPage = 50,
            filters = BreweryFilters(state = "Oregon", type = BreweryType.BREWPUB),
        )
        assertEquals(listOf("Pelican Brewing Company"), oregonBrewpubs.map { it.name })

        val byName = getBreweries(page = 1, perPage = 50, filters = BreweryFilters(query = "deschutes"))
        assertEquals(listOf("Deschutes Brewery"), byName.map { it.name })
    }

    @Test
    fun badgeCache_holdsLastValue() {
        val cache = FilterBadgeCache()
        assertFalse(cache.showBadge.value)
        cache.update(showBadge = true)
        assertTrue(cache.showBadge.value)
        cache.update(showBadge = false)
        assertFalse(cache.showBadge.value)
    }
}
