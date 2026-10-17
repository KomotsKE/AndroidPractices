package com.example.androidpractices.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.androidpractices.domain.model.BreweryFilters
import com.example.androidpractices.domain.model.BreweryType
import com.example.androidpractices.domain.repository.FiltersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Фильтры списка в Preferences DataStore — сохраняются между запусками приложения. */
class FiltersRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : FiltersRepository {

    override val filters: Flow<BreweryFilters> = dataStore.data
        // Повреждённый/нечитаемый файл — не падаем, а начинаем с фильтров по умолчанию.
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            BreweryFilters(
                query = prefs[KEY_QUERY].orEmpty(),
                state = prefs[KEY_STATE]?.takeIf { it.isNotBlank() },
                type = prefs[KEY_TYPE]
                    ?.let { BreweryType.fromApi(it) }
                    ?.takeIf { it != BreweryType.UNKNOWN },
            )
        }
        .distinctUntilChanged()

    override suspend fun save(filters: BreweryFilters) {
        dataStore.edit { prefs ->
            prefs.putOrRemove(KEY_QUERY, filters.query.trim())
            prefs.putOrRemove(KEY_STATE, filters.state)
            prefs.putOrRemove(KEY_TYPE, filters.type?.apiValue)
        }
    }

    private fun MutablePreferences.putOrRemove(
        key: Preferences.Key<String>,
        value: String?,
    ) {
        if (value.isNullOrBlank()) {
            remove(key)
        } else {
            this[key] = value
        }
    }

    companion object {
        const val DATASTORE_NAME = "brewery_filters"

        private val KEY_QUERY = stringPreferencesKey("query")
        private val KEY_STATE = stringPreferencesKey("state")
        private val KEY_TYPE = stringPreferencesKey("type")
    }
}
