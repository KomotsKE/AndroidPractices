package com.example.androidpractices.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SportsBar
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.androidpractices.domain.model.BreweryType

/** Визуальный стиль типа пивоварни: градиент и иконка. */
data class BreweryTypeStyle(
    val light: Color,
    val dark: Color,
    val icon: ImageVector,
) {
    val brush: Brush get() = Brush.linearGradient(listOf(light, dark))
}

val BreweryType.style: BreweryTypeStyle
    get() = when (this) {
        BreweryType.MICRO -> BreweryTypeStyle(Color(0xFFF59E0B), Color(0xFFB45309), Icons.Rounded.SportsBar)
        BreweryType.NANO -> BreweryTypeStyle(Color(0xFF84CC16), Color(0xFF3F6212), Icons.Rounded.WaterDrop)
        BreweryType.REGIONAL -> BreweryTypeStyle(Color(0xFF38BDF8), Color(0xFF0369A1), Icons.Rounded.Apartment)
        BreweryType.BREWPUB -> BreweryTypeStyle(Color(0xFFFB923C), Color(0xFFC2410C), Icons.Rounded.Restaurant)
        BreweryType.LARGE -> BreweryTypeStyle(Color(0xFF818CF8), Color(0xFF4338CA), Icons.Rounded.Factory)
        BreweryType.PLANNING -> BreweryTypeStyle(Color(0xFF94A3B8), Color(0xFF475569), Icons.Rounded.Construction)
        BreweryType.BAR -> BreweryTypeStyle(Color(0xFFF472B6), Color(0xFFBE185D), Icons.Rounded.LocalBar)
        BreweryType.CONTRACT -> BreweryTypeStyle(Color(0xFF2DD4BF), Color(0xFF0F766E), Icons.Rounded.Handshake)
        BreweryType.PROPRIETOR -> BreweryTypeStyle(Color(0xFFC084FC), Color(0xFF7E22CE), Icons.Rounded.Key)
        BreweryType.CLOSED -> BreweryTypeStyle(Color(0xFFA8A29E), Color(0xFF57534E), Icons.Rounded.Block)
        BreweryType.UNKNOWN -> BreweryTypeStyle(Color(0xFF94A3B8), Color(0xFF475569), Icons.Rounded.QuestionMark)
    }
