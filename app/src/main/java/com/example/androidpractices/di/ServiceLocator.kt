package com.example.androidpractices.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.example.androidpractices.data.cache.FilterBadgeCache
import com.example.androidpractices.data.local.BreweryDatabase
import com.example.androidpractices.data.remote.BreweryApi
import com.example.androidpractices.data.repository.BreweryRepositoryImpl
import com.example.androidpractices.data.repository.FavoritesRepositoryImpl
import com.example.androidpractices.data.repository.FiltersRepositoryImpl
import com.example.androidpractices.domain.repository.BreweryRepository
import com.example.androidpractices.domain.repository.FavoritesRepository
import com.example.androidpractices.domain.repository.FiltersRepository
import com.example.androidpractices.domain.usecase.GetBreweriesUseCase
import com.example.androidpractices.domain.usecase.GetBreweryDetailsUseCase
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** DataStore должен быть единственным на файл — поэтому делегат на уровне файла. */
private val Context.filtersDataStore: DataStore<Preferences> by preferencesDataStore(
    name = FiltersRepositoryImpl.DATASTORE_NAME,
)

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

    private val database: BreweryDatabase by lazy {
        Room.databaseBuilder(appContext, BreweryDatabase::class.java, BreweryDatabase.NAME).build()
    }

    val breweryRepository: BreweryRepository by lazy { BreweryRepositoryImpl(breweryApi) }
    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepositoryImpl(database.favoritesDao()) }
    val filtersRepository: FiltersRepository by lazy { FiltersRepositoryImpl(appContext.filtersDataStore) }

    /** Общий для экранов списка и фильтров кэш: показывать ли бейдж. Один на процесс. */
    val filterBadgeCache: FilterBadgeCache by lazy { FilterBadgeCache() }

    val getBreweries by lazy { GetBreweriesUseCase(breweryRepository) }
    val getBreweryDetails by lazy { GetBreweryDetailsUseCase(breweryRepository) }
}
