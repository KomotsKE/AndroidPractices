package com.example.androidpractices.data.remote

import com.example.androidpractices.domain.model.Brewery
import com.example.androidpractices.domain.model.BreweryType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Модель ответа Open Brewery DB (https://www.openbrewerydb.org/documentation).
 * Все поля, кроме id и name, API может вернуть как null.
 */
@Serializable
data class BreweryDto(
    val id: String,
    val name: String,
    @SerialName("brewery_type") val breweryType: String? = null,
    @SerialName("address_1") val address1: String? = null,
    @SerialName("address_2") val address2: String? = null,
    @SerialName("address_3") val address3: String? = null,
    val city: String? = null,
    @SerialName("state_province") val stateProvince: String? = null,
    @SerialName("postal_code") val postalCode: String? = null,
    val country: String? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val phone: String? = null,
    @SerialName("website_url") val websiteUrl: String? = null,
)

fun BreweryDto.toDomain(): Brewery = Brewery(
    id = id,
    name = name.trim(),
    type = BreweryType.fromApi(breweryType),
    street = listOfNotNull(address1, address2, address3)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(", ")
        .ifEmpty { null },
    city = city.orEmpty(),
    stateProvince = stateProvince.orEmpty(),
    postalCode = postalCode?.takeIf { it.isNotBlank() },
    country = country ?: "United States",
    latitude = latitude,
    longitude = longitude,
    phone = phone?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() },
    websiteUrl = websiteUrl?.takeIf { it.isNotBlank() },
)
