package com.example.androidpractices

import com.example.androidpractices.data.model.BreweryType
import com.example.androidpractices.data.repository.MockBreweries
import com.example.androidpractices.data.repository.MockBreweryRepository
import com.example.androidpractices.ui.common.formatPhone
import com.example.androidpractices.ui.common.monogramOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BreweryDataTest {

    private val repository = MockBreweryRepository(networkDelayMs = 0)

    @Test
    fun pagination_returnsAllItemsWithoutDuplicates() = runBlocking {
        val collected = generateSequence(1) { it + 1 }
            .map { page -> runBlocking { repository.getBreweries(page = page, perPage = 10) } }
            .takeWhile { it.isNotEmpty() }
            .flatten()
            .toList()
        assertEquals(MockBreweries.all.size, collected.size)
        assertEquals(collected.size, collected.map { it.id }.toSet().size)
    }

    @Test
    fun filters_byTypeAndQuery() = runBlocking {
        val micro = repository.getBreweries(page = 1, perPage = 50, type = BreweryType.MICRO)
        assertTrue(micro.isNotEmpty())
        assertTrue(micro.all { it.type == BreweryType.MICRO })

        val california = repository.getBreweries(page = 1, perPage = 50, query = "califor")
        assertTrue(california.isNotEmpty())
        assertTrue(california.all { it.stateProvince == "California" })
    }

    @Test
    fun details_foundByIdAndNullForUnknown() = runBlocking {
        val id = MockBreweries.all.first().id
        assertNotNull(repository.getBrewery(id))
        assertNull(repository.getBrewery("missing"))
    }

    @Test
    fun formatters() {
        assertEquals("+1 (530) 555-0101", formatPhone("5305550101"))
        assertEquals("+1 (530) 555-0101", formatPhone("15305550101"))
        assertEquals("3F", monogramOf("3 Floyds Brewing"))
        assertEquals("TA", monogramOf("The Alchemist"))
    }
}
