package com.example.androidpractices.ui.breweries.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidpractices.domain.model.Brewery
import com.example.androidpractices.domain.model.BreweryType
import com.example.androidpractices.di.ServiceLocator
import com.example.androidpractices.ui.common.BreweryAvatar
import com.example.androidpractices.ui.common.BreweryTypeBadge
import com.example.androidpractices.ui.common.MessageState
import com.example.androidpractices.ui.common.displayHost
import com.example.androidpractices.ui.common.formatCoordinates
import com.example.androidpractices.ui.common.formatPhone
import com.example.androidpractices.ui.common.locationLine
import com.example.androidpractices.ui.common.openDialer
import com.example.androidpractices.ui.common.openMap
import com.example.androidpractices.ui.common.openWebsite
import com.example.androidpractices.ui.common.style
import com.example.androidpractices.ui.theme.AndroidPracticesTheme

@Composable
fun BreweryDetailRoute(
    onBack: () -> Unit,
    viewModel: BreweryDetailViewModel = viewModel {
        BreweryDetailViewModel(
            savedStateHandle = createSavedStateHandle(),
            getBreweryDetails = ServiceLocator.getBreweryDetails,
            favoritesRepository = ServiceLocator.favoritesRepository,
        )
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BreweryDetailScreen(
        state = state,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onRetry = viewModel::retry,
    )
}

@Composable
fun BreweryDetailScreen(
    state: BreweryDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "detailState",
        ) { current ->
            when (current) {
                BreweryDetailUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                BreweryDetailUiState.NotFound -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    MessageState(
                        icon = Icons.Rounded.SearchOff,
                        title = "Пивоварня не найдена",
                        message = "Возможно, она была удалена из каталога",
                        actionLabel = "Назад к списку",
                        onAction = onBack,
                    )
                }

                is BreweryDetailUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    MessageState(
                        icon = Icons.Rounded.CloudOff,
                        title = "Не удалось загрузить",
                        message = current.message,
                        actionLabel = "Повторить",
                        onAction = onRetry,
                    )
                }

                is BreweryDetailUiState.Content -> BreweryDetailContent(
                    brewery = current.brewery,
                    isOffline = current.isOffline,
                )
            }
        }

        TopActions(
            onBack = onBack,
            isFavorite = (state as? BreweryDetailUiState.Content)?.isFavorite,
            onToggleFavorite = onToggleFavorite,
        )
    }
}

/** Кнопки поверх шапки: «назад» и «в избранное». */
@Composable
private fun TopActions(
    onBack: () -> Unit,
    isFavorite: Boolean?,
    onToggleFavorite: () -> Unit,
) {
    val colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = Color.Black.copy(alpha = 0.28f),
        contentColor = Color.White,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        FilledIconButton(onClick = onBack, colors = colors) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад")
        }
        Spacer(Modifier.weight(1f))
        if (isFavorite != null) {
            FilledIconButton(onClick = onToggleFavorite, colors = colors) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isFavorite) "Убрать из избранного" else "В избранное",
                    tint = if (isFavorite) Color(0xFFFF8A80) else Color.White,
                )
            }
        }
    }
}

@Composable
private fun BreweryDetailContent(brewery: Brewery, isOffline: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailHeader(brewery)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (isOffline) OfflineBanner()
            AddressSection(brewery)
            ContactsSection(brewery)
            if (brewery.latitude != null && brewery.longitude != null) {
                LocationSection(brewery.name, brewery.latitude, brewery.longitude)
            }
            TypeSection(brewery.type)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = brewery.id,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

/** Плашка «нет сети — показана сохранённая копия». */
@Composable
private fun OfflineBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Нет подключения — показаны данные, сохранённые в избранном",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

/**
 * Шапка экрана на ConstraintLayout:
 * - градиентный hero на всю ширину;
 * - аватар, центрированный по нижней границе hero (наполовину «свисает»);
 * - бейдж типа справа, выровненный между аватаром и краем экрана;
 * - барьер под аватаром и бейджем, от которого идут название и адрес;
 * - цепочка из трёх кнопок действий одинаковой ширины.
 */
@Composable
private fun DetailHeader(brewery: Brewery) {
    val context = LocalContext.current
    val style = brewery.type.style

    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (hero, heroIcon, avatar, badge, name, location) = createRefs()
        val (callButton, siteButton, routeButton) = createRefs()

        Box(
            modifier = Modifier
                .constrainAs(hero) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.value(220.dp)
                }
                .background(style.brush),
        )

        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.16f),
            modifier = Modifier
                .constrainAs(heroIcon) {
                    end.linkTo(hero.end, margin = 8.dp)
                    linkTo(top = hero.top, bottom = hero.bottom, bias = 0.65f)
                }
                .size(160.dp)
                .rotate(-14f),
        )

        Box(
            modifier = Modifier
                .constrainAs(avatar) {
                    start.linkTo(parent.start, margin = 20.dp)
                    // Центр аватара — ровно на нижней границе hero.
                    linkTo(top = hero.bottom, bottom = hero.bottom)
                }
                .size(104.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(5.dp),
        ) {
            BreweryAvatar(
                name = brewery.name,
                type = brewery.type,
                size = 94.dp,
                shape = RoundedCornerShape(27.dp),
                fontSize = 34.sp,
                modifier = Modifier.border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(27.dp)),
            )
        }

        BreweryTypeBadge(
            type = brewery.type,
            modifier = Modifier.constrainAs(badge) {
                top.linkTo(hero.bottom, margin = 12.dp)
                linkTo(
                    start = avatar.end,
                    end = parent.end,
                    startMargin = 12.dp,
                    endMargin = 20.dp,
                    bias = 1f,
                )
                width = Dimension.preferredWrapContent
            },
        )

        val headerBottom = createBottomBarrier(avatar, badge)

        Text(
            text = brewery.name,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.constrainAs(name) {
                top.linkTo(headerBottom, margin = 14.dp)
                linkTo(start = parent.start, end = parent.end, startMargin = 20.dp, endMargin = 20.dp)
                width = Dimension.fillToConstraints
            },
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.constrainAs(location) {
                top.linkTo(name.bottom, margin = 6.dp)
                linkTo(start = name.start, end = name.end)
                width = Dimension.fillToConstraints
            },
        ) {
            Icon(
                imageVector = Icons.Rounded.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = listOf(locationLine(brewery.city, brewery.stateProvince), brewery.country)
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val actionsChain = createHorizontalChain(
            callButton, siteButton, routeButton,
            chainStyle = ChainStyle.Spread,
        )
        constrain(actionsChain) {
            start.linkTo(parent.start, margin = 16.dp)
            end.linkTo(parent.end, margin = 16.dp)
        }

        val phone = brewery.phone
        QuickAction(
            icon = Icons.Rounded.Call,
            label = "Позвонить",
            enabled = phone != null,
            onClick = { phone?.let(context::openDialer) },
            modifier = Modifier.constrainAs(callButton) {
                top.linkTo(location.bottom, margin = 20.dp)
                bottom.linkTo(parent.bottom, margin = 8.dp)
                width = Dimension.fillToConstraints
            },
        )
        val website = brewery.websiteUrl
        QuickAction(
            icon = Icons.Rounded.Language,
            label = "Сайт",
            enabled = website != null,
            onClick = { website?.let(context::openWebsite) },
            modifier = Modifier.constrainAs(siteButton) {
                top.linkTo(callButton.top)
                width = Dimension.fillToConstraints
            },
        )
        val lat = brewery.latitude
        val lon = brewery.longitude
        QuickAction(
            icon = Icons.Rounded.Directions,
            label = "Маршрут",
            enabled = lat != null && lon != null,
            onClick = { if (lat != null && lon != null) context.openMap(lat, lon, brewery.name) },
            modifier = Modifier.constrainAs(routeButton) {
                top.linkTo(callButton.top)
                width = Dimension.fillToConstraints
            },
        )
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .padding(horizontal = 4.dp)
            .alpha(if (enabled) 1f else 0.45f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AddressSection(brewery: Brewery) {
    InfoCard(title = "Адрес") {
        InfoRow(icon = Icons.Rounded.Home, label = "Улица", value = brewery.street)
        InfoDivider()
        InfoRow(
            icon = Icons.Rounded.LocationCity,
            label = "Город и штат",
            value = listOfNotNull(
                locationLine(brewery.city, brewery.stateProvince).ifBlank { null },
                brewery.postalCode,
            ).joinToString(" ").ifBlank { null },
        )
        InfoDivider()
        InfoRow(icon = Icons.Rounded.Public, label = "Страна", value = brewery.country)
    }
}

@Composable
private fun ContactsSection(brewery: Brewery) {
    val context = LocalContext.current
    val phone = brewery.phone
    val website = brewery.websiteUrl
    InfoCard(title = "Контакты") {
        InfoRow(
            icon = Icons.Rounded.Phone,
            label = "Телефон",
            value = phone?.let(::formatPhone),
            onClick = phone?.let { { context.openDialer(it) } },
        )
        InfoDivider()
        InfoRow(
            icon = Icons.Rounded.Language,
            label = "Сайт",
            value = website?.let(::displayHost),
            onClick = website?.let { { context.openWebsite(it) } },
        )
    }
}

@Composable
private fun LocationSection(name: String, latitude: Double, longitude: Double) {
    val context = LocalContext.current
    InfoCard(title = "На карте") {
        MapPreview(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { context.openMap(latitude, longitude, name) },
        )
        InfoRow(
            icon = Icons.Rounded.MyLocation,
            label = "Координаты",
            value = formatCoordinates(latitude, longitude),
            onClick = { context.openMap(latitude, longitude, name) },
        )
    }
}

/** Стилизованная «карта» — настоящая появится в одной из следующих практик. */
@Composable
private fun MapPreview(modifier: Modifier = Modifier) {
    val base = MaterialTheme.colorScheme.tertiaryContainer
    val lines = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.10f)
    val road = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    val pin = MaterialTheme.colorScheme.primary
    Box(modifier = modifier.background(base), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 22.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(lines, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(lines, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                y += step
            }
            drawLine(road, Offset(0f, size.height * 0.7f), Offset(size.width, size.height * 0.35f), strokeWidth = 10.dp.toPx())
            drawLine(road, Offset(size.width * 0.3f, 0f), Offset(size.width * 0.55f, size.height), strokeWidth = 7.dp.toPx())
            drawCircle(pin.copy(alpha = 0.18f), radius = 30.dp.toPx(), center = center)
        }
        Icon(
            imageVector = Icons.Rounded.Place,
            contentDescription = null,
            tint = pin,
            modifier = Modifier.size(40.dp).padding(bottom = 6.dp),
        )
    }
}

@Composable
private fun TypeSection(type: BreweryType) {
    InfoCard(title = "Тип пивоварни") {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(type.style.brush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(type.style.icon, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(type.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String?,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value ?: "Не указано",
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    value == null -> MaterialTheme.colorScheme.outline
                    onClick != null -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun InfoDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    )
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun BreweryDetailPreview() {
    AndroidPracticesTheme {
        BreweryDetailScreen(
            state = BreweryDetailUiState.Content(
                brewery = Brewery(
                    id = "f32ec570-d2a0-5fd3-bc32-1d9122dc6f87",
                    name = "Sierra Nevada Brewing Co.",
                    type = BreweryType.REGIONAL,
                    street = "1075 E 20th St",
                    city = "Chico",
                    stateProvince = "California",
                    postalCode = "95928",
                    country = "United States",
                    latitude = 39.7246,
                    longitude = -121.8156,
                    phone = "5305550101",
                    websiteUrl = "https://sierranevada.com",
                ),
                isFavorite = true,
            ),
            onBack = {},
            onToggleFavorite = {},
            onRetry = {},
        )
    }
}
