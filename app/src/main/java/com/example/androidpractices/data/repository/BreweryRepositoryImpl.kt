package com.example.androidpractices.data.repository

import com.example.androidpractices.data.remote.BreweryApi
import com.example.androidpractices.data.remote.toDomain
import com.example.androidpractices.data.remote.toDomainException
import com.example.androidpractices.domain.model.Brewery
import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.model.BreweryType
import com.example.androidpractices.domain.repository.BreweryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Сетевая реализация. Работа (запрос, разбор JSON, маппинг) выполняется на [ioDispatcher],
 * любые сбои приходят наружу как [com.example.androidpractices.domain.model.BreweryException].
 */
class BreweryRepositoryImpl(
    private val api: BreweryApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BreweryRepository {

    override suspend fun getBreweries(
        page: Int,
        perPage: Int,
        query: String?,
        type: BreweryType?,
        state: String?,
    ): List<BreweryListItem> {
        require(page >= 1) { "page starts from 1" }
        return safeCall {
            api.getBreweries(
                page = page,
                perPage = perPage,
                byName = query?.trim()?.takeIf { it.isNotEmpty() },
                byType = type?.apiValue,
                byState = state?.toStateParam(),
            ).map { it.toDomain().toListItem() }
        }
    }

    override suspend fun getBrewery(id: String): Brewery? = safeCall {
        try {
            api.getBrewery(id).toDomain()
        } catch (e: HttpException) {
            if (e.code() == 404) null else throw e
        }
    }

    override suspend fun getBreweriesByIds(ids: List<String>): List<BreweryListItem> {
        if (ids.isEmpty()) return emptyList()
        return safeCall {
            val byId = api.getBreweries(
                page = 1,
                perPage = ids.size.coerceAtMost(MAX_PER_PAGE),
                byIds = ids.joinToString(","),
            ).associateBy { it.id }
            // Сервер порядок не гарантирует — возвращаем в порядке запрошенных id.
            ids.mapNotNull { byId[it]?.toDomain()?.toListItem() }
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): T = withContext(ioDispatcher) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    /** «New York» → «new_york»: так API ожидает пробелы в названии штата. */
    private fun String.toStateParam(): String? =
        trim().takeIf { it.isNotEmpty() }?.lowercase()?.replace(' ', '_')

    private companion object {
        const val MAX_PER_PAGE = 200
    }
}
