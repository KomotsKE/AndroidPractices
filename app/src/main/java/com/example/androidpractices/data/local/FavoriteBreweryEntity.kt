package com.example.androidpractices.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androidpractices.domain.model.Brewery
import com.example.androidpractices.domain.model.BreweryType

/**
 * Избранная пивоварня. Храним все поля экрана деталей, чтобы и список избранного,
 * и сами детали открывались без интернета.
 */
@Entity(tableName = "favorite_breweries")
data class FavoriteBreweryEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** [BreweryType.apiValue] — устойчиво к переименованию констант enum. */
    val type: String,
    val street: String?,
    val city: String,
    @ColumnInfo(name = "state_province") val stateProvince: String,
    @ColumnInfo(name = "postal_code") val postalCode: String?,
    val country: String,
    val latitude: Double?,
    val longitude: Double?,
    val phone: String?,
    @ColumnInfo(name = "website_url") val websiteUrl: String?,
    /** Время добавления — для сортировки «новые сверху». */
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

fun FavoriteBreweryEntity.toDomain(): Brewery = Brewery(
    id = id,
    name = name,
    type = BreweryType.fromApi(type),
    street = street,
    city = city,
    stateProvince = stateProvince,
    postalCode = postalCode,
    country = country,
    latitude = latitude,
    longitude = longitude,
    phone = phone,
    websiteUrl = websiteUrl,
)

fun Brewery.toEntity(addedAt: Long): FavoriteBreweryEntity = FavoriteBreweryEntity(
    id = id,
    name = name,
    type = type.apiValue,
    street = street,
    city = city,
    stateProvince = stateProvince,
    postalCode = postalCode,
    country = country,
    latitude = latitude,
    longitude = longitude,
    phone = phone,
    websiteUrl = websiteUrl,
    addedAt = addedAt,
)
