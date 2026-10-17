package com.example.androidpractices.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidpractices.di.ServiceLocator
import com.example.androidpractices.domain.model.BreweryFilters
import com.example.androidpractices.domain.model.BreweryType
import com.example.androidpractices.domain.model.UsStates
import com.example.androidpractices.ui.common.style
import com.example.androidpractices.ui.theme.AndroidPracticesTheme
import kotlinx.coroutines.launch

@Composable
fun FiltersRoute(
    onClose: () -> Unit,
    viewModel: FiltersViewModel = viewModel {
        FiltersViewModel(ServiceLocator.filtersRepository, ServiceLocator.filterBadgeCache)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // «Готово» сохраняет фильтры, а закрываем экран только после записи в DataStore.
    LaunchedEffect(state.isDone) {
        if (state.isDone) onClose()
    }

    FiltersScreen(
        state = state,
        onClose = onClose,
        onQueryChange = viewModel::onQueryChange,
        onStateSelected = viewModel::onStateSelected,
        onTypeSelected = viewModel::onTypeSelected,
        onReset = viewModel::reset,
        onApply = viewModel::apply,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    state: FiltersUiState,
    onClose: () -> Unit,
    onQueryChange: (String) -> Unit,
    onStateSelected: (String?) -> Unit,
    onTypeSelected: (BreweryType?) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    var showStatePicker by rememberSaveable { mutableStateOf(false) }
    val draft = state.draft

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фильтры") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = "Закрыть без сохранения")
                    }
                },
                actions = {
                    TextButton(onClick = onReset, enabled = state.isLoaded && !draft.isDefault) {
                        Text("Сбросить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = onApply,
                    enabled = state.isLoaded && !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when {
                                draft.isDefault -> "Готово · все пивоварни"
                                else -> "Готово · фильтров: ${draft.activeCount}"
                            },
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        if (!state.isLoaded) {
            Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            FilterSection(
                icon = Icons.Rounded.Search,
                title = "Название",
                caption = "Ищет совпадение в названии пивоварни",
            ) {
                NameField(query = draft.query, onQueryChange = onQueryChange)
            }

            FilterSection(
                icon = Icons.Rounded.Place,
                title = "Штат",
                caption = "Только пивоварни выбранного штата",
            ) {
                StateField(
                    selected = draft.state,
                    onClick = { showStatePicker = true },
                    onClear = { onStateSelected(null) },
                )
            }

            FilterSection(
                icon = Icons.Rounded.Category,
                title = "Тип пивоварни",
                caption = draft.type?.description ?: "Пивоварни любого типа",
            ) {
                TypeChips(selected = draft.type, onSelect = onTypeSelected)
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showStatePicker) {
        StatePickerSheet(
            selected = draft.state,
            onSelect = onStateSelected,
            onDismiss = { showStatePicker = false },
        )
    }
}

@Composable
private fun FilterSection(
    icon: ImageVector,
    title: String,
    caption: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        content()
    }
}

@Composable
private fun NameField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Например, Sierra Nevada") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Очистить название")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
}

@Composable
private fun StateField(
    selected: String?,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp).heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (selected == null) Icons.Rounded.Public else Icons.Rounded.Place,
                contentDescription = null,
                tint = if (selected == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = selected ?: "Все штаты",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
            )
            if (selected != null) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Rounded.Close, contentDescription = "Убрать штат")
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeChips(selected: BreweryType?, onSelect: (BreweryType?) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
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
        BreweryType.filterable.forEach { type ->
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

/** Выбор штата: шторка с поиском по списку. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatePickerSheet(
    selected: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var search by rememberSaveable { mutableStateOf("") }
    val states = remember(search) {
        val needle = search.trim()
        UsStates.all.filter { it.contains(needle, ignoreCase = true) }
    }

    val pick: (String?) -> Unit = { value ->
        onSelect(value)
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            text = "Штат",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Найти штат") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
        LazyColumn(modifier = Modifier.heightIn(max = 480.dp)) {
            if (search.isBlank()) {
                item(key = "all") {
                    StateRow(title = "Все штаты", isSelected = selected == null, onClick = { pick(null) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
            items(states, key = { it }) { usState ->
                StateRow(title = usState, isSelected = usState == selected, onClick = { pick(usState) })
            }
            if (states.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = "Такого штата нет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
            item(key = "bottom") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StateRow(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        ListItem(
            headlineContent = { Text(title) },
            trailingContent = if (isSelected) {
                { Icon(Icons.Rounded.Check, contentDescription = "Выбрано", tint = MaterialTheme.colorScheme.primary) }
            } else {
                null
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun FiltersScreenPreview() {
    AndroidPracticesTheme {
        FiltersScreen(
            state = FiltersUiState(
                draft = BreweryFilters(query = "river", state = "California", type = BreweryType.BREWPUB),
                isLoaded = true,
            ),
            onClose = {},
            onQueryChange = {},
            onStateSelected = {},
            onTypeSelected = {},
            onReset = {},
            onApply = {},
        )
    }
}
