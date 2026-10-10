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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidpractices.data.model.BreweryListItem
import com.example.androidpractices.data.model.BreweryType
import com.example.androidpractices.di.ServiceLocator
import com.example.androidpractices.ui.common.BreweryCard
import com.example.androidpractices.ui.common.MessageState
import com.example.androidpractices.ui.common.style
import com.example.androidpractices.ui.theme.AndroidPracticesTheme
import kotlinx.coroutines.launch

/** Точка входа экрана: достаёт ViewModel и связывает её с UI. */
@Composable
fun BreweryListRoute(
    onBreweryClick: (String) -> Unit,
    viewModel: BreweryListViewModel = viewModel {
        BreweryListViewModel(ServiceLocator.breweryRepository, ServiceLocator.favoritesRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BreweryListScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onTypeSelected = viewModel::onTypeSelected,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::retry,
        onBreweryClick = onBreweryClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreweryListScreen(
    state: BreweryListUiState,
    onQueryChange: (String) -> Unit,
    onTypeSelected: (BreweryType?) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onBreweryClick: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val scrollToTop: () -> Unit = { scope.launch { listState.scrollToItem(0) } }

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
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                        ),
                    )
                    SearchField(
                        query = state.query,
                        onQueryChange = {
                            onQueryChange(it)
                            scrollToTop()
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    TypeFilterRow(
                        selected = state.selectedType,
                        onSelect = {
                            onTypeSelected(it)
                            scrollToTop()
                        },
                    )
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
                    message = "Попробуйте изменить запрос или выбрать другой тип пивоварни",
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

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Название, город или штат") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Очистить поиск")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun TypeFilterRow(
    selected: BreweryType?,
    onSelect: (BreweryType?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Все") },
                leadingIcon = if (selected == null) {
                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
            )
        }
        items(items = BreweryType.filterable, key = { it.apiValue }) { type ->
            val isSelected = type == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(if (isSelected) null else type) },
                label = { Text(type.title) },
                leadingIcon = {
                    Icon(
                        imageVector = if (isSelected) Icons.Rounded.Check else type.style.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }
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
                isLoading = false,
                items = listOf(
                    BreweryListItem("1", "Sierra Nevada Brewing Co.", BreweryType.REGIONAL, "Chico", "California", "United States"),
                    BreweryListItem("2", "Tree House Brewing Company", BreweryType.MICRO, "Charlton", "Massachusetts", "United States"),
                    BreweryListItem("3", "Taproom 42", BreweryType.BAR, "Denver", "Colorado", "United States"),
                ),
                endReached = true,
            ),
            onQueryChange = {},
            onTypeSelected = {},
            onLoadMore = {},
            onRetry = {},
            onBreweryClick = {},
        )
    }
}
