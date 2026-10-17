package com.example.androidpractices.ui.breweries.list

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidpractices.domain.model.BreweryFilters
import com.example.androidpractices.domain.model.BreweryListItem
import com.example.androidpractices.domain.model.BreweryType
import com.example.androidpractices.di.ServiceLocator
import com.example.androidpractices.ui.common.BreweryCard
import com.example.androidpractices.ui.common.MessageState
import com.example.androidpractices.ui.common.style
import com.example.androidpractices.ui.theme.AndroidPracticesTheme

/** Точка входа экрана: достаёт ViewModel и связывает её с UI. */
@Composable
fun BreweryListRoute(
    onBreweryClick: (String) -> Unit,
    onOpenFilters: () -> Unit,
    viewModel: BreweryListViewModel = viewModel {
        BreweryListViewModel(
            getBreweries = ServiceLocator.getBreweries,
            favoritesRepository = ServiceLocator.favoritesRepository,
            filtersRepository = ServiceLocator.filtersRepository,
            badgeCache = ServiceLocator.filterBadgeCache,
        )
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BreweryListScreen(
        state = state,
        onOpenFilters = onOpenFilters,
        onClearQuery = viewModel::clearQuery,
        onClearState = viewModel::clearState,
        onClearType = viewModel::clearType,
        onResetFilters = viewModel::resetFilters,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::retry,
        onBreweryClick = onBreweryClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreweryListScreen(
    state: BreweryListUiState,
    onOpenFilters: () -> Unit,
    onClearQuery: () -> Unit,
    onClearState: () -> Unit,
    onClearType: () -> Unit,
    onResetFilters: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onBreweryClick: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    // Новые фильтры — новый список, начинаем сверху. Ключ сохраняем, чтобы при возврате
    // с экрана деталей (экран заново входит в композицию) не сбрасывать прокрутку.
    var shownFiltersKey by rememberSaveable { mutableStateOf(state.filters.toString()) }
    LaunchedEffect(state.filters) {
        val key = state.filters.toString()
        if (key != shownFiltersKey) {
            shownFiltersKey = key
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Column {
                    TopAppBar(
                        title = { Text("Пивоварни США") },
                        actions = {
                            FiltersButton(showBadge = state.showFilterBadge, onClick = onOpenFilters)
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                        ),
                    )
                    SearchPill(
                        filters = state.filters,
                        onClick = onOpenFilters,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    if (state.filters.isDefault) {
                        Spacer(Modifier.height(8.dp))
                    } else {
                        ActiveFiltersRow(
                            filters = state.filters,
                            onClearQuery = onClearQuery,
                            onClearState = onClearState,
                            onClearType = onClearType,
                            onReset = onResetFilters,
                        )
                    }
                    if (state.isLoading && state.items.isNotEmpty()) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(3.dp))
                    } else {
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        },
    ) { innerPadding ->
        BreweryList(
            state = state,
            listState = listState,
            contentPadding = innerPadding,
            onLoadMore = onLoadMore,
            onRetry = onRetry,
            onResetFilters = onResetFilters,
            onBreweryClick = onBreweryClick,
        )
    }
}

@Composable
private fun BreweryList(
    state: BreweryListUiState,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onResetFilters: () -> Unit,
    onBreweryClick: (String) -> Unit,
) {
    // Подготовка к пагинации: когда до конца списка осталось 3 элемента — просим следующую страницу.
    val shouldLoadMore by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            info.totalItemsCount > 0 && (lastVisible >= info.totalItemsCount - 3)
        }
    }
    LaunchedEffect(shouldLoadMore, state.items.size) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            state.items.isNotEmpty() -> {
                items(items = state.items, key = { it.id }, contentType = { "brewery" }) { brewery ->
                    BreweryCard(
                        brewery = brewery,
                        isFavorite = brewery.id in state.favoriteIds,
                        onClick = { onBreweryClick(brewery.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
                item(key = "footer", contentType = "footer") {
                    ListFooter(state = state, onRetry = onRetry)
                }
            }

            state.isLoading -> {
                items(count = 6, key = { "skeleton_$it" }, contentType = { "skeleton" }) {
                    SkeletonCard()
                }
            }

            state.errorMessage != null -> item(key = "error") {
                MessageState(
                    icon = Icons.Rounded.CloudOff,
                    title = "Что-то пошло не так",
                    message = state.errorMessage,
                    actionLabel = "Повторить",
                    onAction = onRetry,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(top = 48.dp),
                )
            }

            else -> item(key = "empty") {
                MessageState(
                    icon = Icons.Rounded.SearchOff,
                    title = "Ничего не нашлось",
                    message = "Под выбранные фильтры не подходит ни одна пивоварня. Попробуйте ослабить условия",
                    actionLabel = if (state.filters.isDefault) null else "Сбросить фильтры",
                    onAction = if (state.filters.isDefault) null else onResetFilters,
                    modifier = Modifier.fillParentMaxWidth().padding(top = 48.dp),
                )
            }
        }
    }
}

@Composable
private fun ListFooter(state: BreweryListUiState, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.isLoadingMore -> CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)

            state.errorMessage != null -> TextButton(onClick = onRetry) {
                Text("Не удалось загрузить. Повторить")
            }

            state.endReached -> Text(
                text = "Показаны все пивоварни: ${state.items.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Кнопка фильтров с бейджем-точкой, если фильтры отличаются от значений по умолчанию. */
@Composable
private fun FiltersButton(showBadge: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (showBadge) {
                    Badge(containerColor = MaterialTheme.colorScheme.error)
                }
            },
        ) {
            Icon(Icons.Rounded.Tune, contentDescription = if (showBadge) "Фильтры (изменены)" else "Фильтры")
        }
    }
}

/** «Строка поиска», которая открывает экран фильтров. */
@Composable
private fun SearchPill(
    filters: BreweryFilters,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = filters.query.ifBlank { "Название, штат или тип" },
                style = MaterialTheme.typography.bodyLarge,
                color = if (filters.query.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (!filters.isDefault) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = filters.activeCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}

/** Включённые фильтры чипами: крестик убирает один фильтр, «Сбросить» — все. */
@Composable
private fun ActiveFiltersRow(
    filters: BreweryFilters,
    onClearQuery: () -> Unit,
    onClearState: () -> Unit,
    onClearType: () -> Unit,
    onReset: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (filters.query.isNotBlank()) {
            item(key = "query") {
                ActiveFilterChip(
                    label = "«${filters.query.trim()}»",
                    icon = Icons.Rounded.Search,
                    onRemove = onClearQuery,
                )
            }
        }
        filters.state?.let { usState ->
            item(key = "state") {
                ActiveFilterChip(label = usState, icon = Icons.Rounded.Place, onRemove = onClearState)
            }
        }
        filters.type?.let { type ->
            item(key = "type") {
                ActiveFilterChip(label = type.title, icon = type.style.icon, onRemove = onClearType)
            }
        }
        if (filters.activeCount > 1) {
            item(key = "reset") {
                AssistChip(
                    onClick = onReset,
                    label = { Text("Сбросить все") },
                    leadingIcon = {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                )
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(
    label: String,
    icon: ImageVector,
    onRemove: () -> Unit,
) {
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        trailingIcon = {
            Icon(Icons.Rounded.Close, contentDescription = "Убрать фильтр", modifier = Modifier.size(18.dp))
        },
    )
}

@Composable
private fun SkeletonCard() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 700), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    val block = MaterialTheme.colorScheme.surfaceContainerHighest
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(block))
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(block))
            Box(Modifier.fillMaxWidth(0.45f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(block))
            Box(Modifier.width(96.dp).height(20.dp).clip(RoundedCornerShape(50)).background(block))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BreweryListScreenPreview() {
    AndroidPracticesTheme {
        BreweryListScreen(
            state = BreweryListUiState(
                filters = BreweryFilters(state = "California", type = BreweryType.REGIONAL),
                showFilterBadge = true,
                isLoading = false,
                items = listOf(
                    BreweryListItem("1", "Sierra Nevada Brewing Co.", BreweryType.REGIONAL, "Chico", "California", "United States"),
                    BreweryListItem("2", "Tree House Brewing Company", BreweryType.MICRO, "Charlton", "Massachusetts", "United States"),
                    BreweryListItem("3", "Taproom 42", BreweryType.BAR, "Denver", "Colorado", "United States"),
                ),
                endReached = true,
            ),
            onOpenFilters = {},
            onClearQuery = {},
            onClearState = {},
            onClearType = {},
            onResetFilters = {},
            onLoadMore = {},
            onRetry = {},
            onBreweryClick = {},
        )
    }
}
