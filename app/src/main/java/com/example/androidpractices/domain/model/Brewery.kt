package com.example.androidpractices.domain.model

/**
 * Краткая модель для списка: только то, что нужно показать в карточке.
 */
data class BreweryListItem(
    val id: String,
    val name: String,
    val type: BreweryType,
    val city: String,
    val stateProvince: String,
    val country: String,
)

/**
 * Полная модель для экрана деталей (запрос `GET /v1/breweries/{id}`).
 */
data class Brewery(
    val id: String,
    val name: String,
    val type: BreweryType,
    val street: String?,
    val city: String,
    val stateProvince: String,
    val postalCode: String?,
    val country: String,
    val latitude: Double?,
    val longitude: Double?,
    val phone: String?,
    val websiteUrl: String?,
) {
    val hasCoordinates: Boolean get() = latitude != null && longitude != null

    fun toListItem() = BreweryListItem(
        id = id,
        name = name,
        type = type,
        city = city,
        stateProvince = stateProvince,
        country = country,
    )
}
