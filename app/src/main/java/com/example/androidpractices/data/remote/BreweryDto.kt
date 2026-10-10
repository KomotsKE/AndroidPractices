package com.example.androidpractices.data.remote

import com.example.androidpractices.data.model.Brewery
import com.example.androidpractices.data.model.BreweryType

/**
 * Модель ответа Open Brewery DB (https://www.openbrewerydb.org/documentation).
 * Имена полей в JSON — в snake_case, указаны в комментариях; в следующей практике
 * сюда добавятся аннотации сериализации (@SerialName).
 */
data class BreweryDto(
    val id: String,
    val name: String,
    val breweryType: String?,   // brewery_type
    val address1: String?,      // address_1
    val address2: String? = null, // address_2
    val address3: String? = null, // address_3
    val city: String?,          // city
    val stateProvince: String?, // state_province
    val postalCode: String?,    // postal_code
    val country: String?,       // country
    val longitude: Double?,     // longitude
    val latitude: Double?,      // latitude
    val phone: String?,         // phone
    val websiteUrl: String?,    // website_url
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
