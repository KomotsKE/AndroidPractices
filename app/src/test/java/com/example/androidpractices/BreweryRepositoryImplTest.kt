package com.example.androidpractices

import com.example.androidpractices.data.remote.BreweryApi
import com.example.androidpractices.data.remote.BreweryDto
import com.example.androidpractices.data.repository.BreweryRepositoryImpl
import com.example.androidpractices.domain.model.BreweryException
import com.example.androidpractices.domain.model.BreweryType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class BreweryRepositoryImplTest {

    private class FakeApi(
        var list: (page: Int, perPage: Int, byName: String?, byType: String?, byIds: String?) -> List<BreweryDto> =
            { _, _, _, _, _ -> emptyList() },
        var single: (id: String) -> BreweryDto = { error("not stubbed") },
    ) : BreweryApi {
        var lastQuery: List<String?> = emptyList()
        var lastState: String? = null

        override suspend fun getBreweries(
            page: Int,
            perPage: Int,
            byName: String?,
            byType: String?,
            byIds: String?,
            byState: String?,
        ): List<BreweryDto> {
            lastQuery = listOf(page.toString(), perPage.toString(), byName, byType, byIds)
            lastState = byState
            return list(page, perPage, byName, byType, byIds)
        }

        override suspend fun getBrewery(id: String): BreweryDto = single(id)
    }

    private fun dto(id: String, name: String = "Brewery $id", type: String? = "micro") =
        BreweryDto(id = id, name = name, breweryType = type, city = "Chico", stateProvince = "California")

    private fun repo(api: BreweryApi) = BreweryRepositoryImpl(api, UnconfinedTestDispatcher())

    private fun httpError(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun list_passesQueryAndTypeAsRequestParameters() = runTest {
        val api = FakeApi(list = { _, _, _, _, _ -> listOf(dto("1")) })

        val result = repo(api).getBreweries(page = 2, perPage = 10, query = "  sierra ", type = BreweryType.MICRO)

        assertEquals(listOf("2", "10", "sierra", "micro", null), api.lastQuery)
        assertEquals("1", result.single().id)
    }

    @Test
    fun list_stateIsSentInApiFormat() = runTest {
        val api = FakeApi()

        repo(api).getBreweries(page = 1, perPage = 10, state = " New York ")

        assertEquals("new_york", api.lastState)
    }

    @Test
    fun list_blankQueryAndNoTypeAreOmitted() = runTest {
        val api = FakeApi()

        repo(api).getBreweries(page = 1, perPage = 10, query = "   ", type = null)

        assertEquals(listOf("1", "10", null, null, null), api.lastQuery)
    }

    @Test
    fun details_404ReturnsNull() = runTest {
        val api = FakeApi(single = { throw httpError(404) })
        assertNull(repo(api).getBrewery("missing"))
    }

    @Test
    fun details_serverErrorIsMappedToDomainException() = runTest {
        val api = FakeApi(single = { throw httpError(500) })

        val error = runCatching { repo(api).getBrewery("1") }.exceptionOrNull()

        assertTrue(error is BreweryException.Server)
        assertEquals(500, (error as BreweryException.Server).code)
    }

    @Test
    fun network_failuresAreMappedToDomainExceptions() = runTest {
        val offline = FakeApi(list = { _, _, _, _, _ -> throw UnknownHostException() })
        val slow = FakeApi(list = { _, _, _, _, _ -> throw SocketTimeoutException() })

        val e1 = runCatching { repo(offline).getBreweries(1, 10) }.exceptionOrNull()
        val e2 = runCatching { repo(slow).getBreweries(1, 10) }.exceptionOrNull()

        assertTrue(e1 is BreweryException.NoConnection)
        assertTrue(e2 is BreweryException.Timeout)
    }

    @Test
    fun byIds_keepsRequestedOrderAndSkipsMissing() = runTest {
        val api = FakeApi(list = { _, _, _, _, _ -> listOf(dto("a"), dto("b"), dto("c")) })

        val result = repo(api).getBreweriesByIds(listOf("c", "x", "a"))

        assertEquals(listOf("c", "a"), result.map { it.id })
        assertEquals("c,x,a", api.lastQuery[4])
    }

    @Test
    fun byIds_emptyListDoesNotHitNetwork() = runTest {
        val api = FakeApi(list = { _, _, _, _, _ -> error("must not be called") })
        assertTrue(repo(api).getBreweriesByIds(emptyList()).isEmpty())
    }

    @Test
    fun dto_parsesSnakeCaseNullsAndIgnoresUnknownKeys() {
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }
        val body = """
            {"id":"1","name":"Test","brewery_type":"micro","address_1":"1 Main St",
             "address_2":null,"city":"Chico","state_province":"California",
             "postal_code":"95928","country":"United States","longitude":null,
             "latitude":"39.7","phone":null,"website_url":null,"state":"California"}
        """.trimIndent()

        val dto = json.decodeFromString<BreweryDto>(body)

        assertEquals("micro", dto.breweryType)
        assertEquals("1 Main St", dto.address1)
        assertEquals(39.7, dto.latitude)
        assertNull(dto.longitude)
    }
}
