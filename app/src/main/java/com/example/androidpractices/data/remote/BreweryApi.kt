package com.example.androidpractices.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Open Brewery DB, базовый адрес — [BASE_URL]. */
interface BreweryApi {

    /**
     * Список пивоварен. Необязательные параметры (`null`) в запрос не попадают.
     * @param page номер страницы, начиная с 1
     * @param perPage размер страницы (максимум 200)
     * @param byName поиск по названию
     * @param byType фильтр по типу ([com.example.androidpractices.domain.model.BreweryType.apiValue])
     * @param byIds id через запятую
     */
    @GET("breweries")
    suspend fun getBreweries(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("by_name") byName: String? = null,
        @Query("by_type") byType: String? = null,
        @Query("by_ids") byIds: String? = null,
    ): List<BreweryDto>

    @GET("breweries/{id}")
    suspend fun getBrewery(@Path("id") id: String): BreweryDto

    companion object {
        const val BASE_URL = "https://api.openbrewerydb.org/v1/"
    }
}
