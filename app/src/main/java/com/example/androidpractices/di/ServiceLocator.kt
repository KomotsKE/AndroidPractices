package com.example.androidpractices.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.example.androidpractices.data.remote.BreweryApi
import com.example.androidpractices.data.repository.BreweryRepositoryImpl
import com.example.androidpractices.domain.repository.BreweryRepository
import com.example.androidpractices.domain.repository.FavoritesRepository
import com.example.androidpractices.domain.usecase.GetBreweriesByIdsUseCase
import com.example.androidpractices.domain.usecase.GetBreweriesUseCase
import com.example.androidpractices.domain.usecase.GetBreweryDetailsUseCase
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Простейший DI: единственные экземпляры зависимостей на весь процесс.
 * [init] вызывается из [com.example.androidpractices.BreweryApplication].
 */
object ServiceLocator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            // API отдаёт latitude/longitude строками ("39.72"), isLenient разбирает их как Double
            isLenient = true
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(ChuckerInterceptor.Builder(appContext).build())
            .build()
    }

    private val breweryApi: BreweryApi by lazy {
        Retrofit.Builder()
            .baseUrl(BreweryApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(BreweryApi::class.java)
    }

    val breweryRepository: BreweryRepository by lazy { BreweryRepositoryImpl(breweryApi) }
    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepository() }

    val getBreweries by lazy { GetBreweriesUseCase(breweryRepository) }
    val getBreweryDetails by lazy { GetBreweryDetailsUseCase(breweryRepository) }
    val getBreweriesByIds by lazy { GetBreweriesByIdsUseCase(breweryRepository) }
}
