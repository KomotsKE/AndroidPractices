package com.example.androidpractices.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsBar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsBar
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val ARG_BREWERY_ID = "breweryId"

    // Вкладка «Пивоварни» — вложенный граф со своим стеком: список → детали.
    const val BREWERIES_GRAPH = "breweries"
    const val BREWERY_LIST = "breweries/list"
    const val BREWERY_DETAIL = "breweries/detail/{$ARG_BREWERY_ID}"
    fun breweryDetail(id: String) = "breweries/detail/$id"
    const val BREWERY_FILTERS = "breweries/filters"

    // Вкладка «Избранное» — свой стек, детали открываются внутри вкладки.
    const val FAVORITES_GRAPH = "favorites"
    const val FAVORITES_LIST = "favorites/list"
    const val FAVORITE_DETAIL = "favorites/detail/{$ARG_BREWERY_ID}"
    fun favoriteDetail(id: String) = "favorites/detail/$id"

    const val MAP = "map"
    const val SETTINGS = "settings"
}

/** Вкладки нижней навигации. */
enum class TopLevelDestination(
    /** Маршрут вкладки (граф или экран) — по нему переключаемся. */
    val route: String,
    /** Корневой экран вкладки — к нему возвращаемся при повторном нажатии. */
    val startRoute: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    BREWERIES(
        route = Routes.BREWERIES_GRAPH,
        startRoute = Routes.BREWERY_LIST,
        label = "Пивоварни",
        selectedIcon = Icons.Rounded.SportsBar,
        unselectedIcon = Icons.Outlined.SportsBar,
    ),
    FAVORITES(
        route = Routes.FAVORITES_GRAPH,
        startRoute = Routes.FAVORITES_LIST,
        label = "Избранное",
        selectedIcon = Icons.Rounded.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
    ),
    MAP(
        route = Routes.MAP,
        startRoute = Routes.MAP,
        label = "Карта",
        selectedIcon = Icons.Rounded.Map,
        unselectedIcon = Icons.Outlined.Map,
    ),
    SETTINGS(
        route = Routes.SETTINGS,
        startRoute = Routes.SETTINGS,
        label = "Настройки",
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
}
