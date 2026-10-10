package com.example.androidpractices.ui.common

import java.util.Locale
import kotlin.math.abs

/** "3 Floyds Brewing" → "3F", "The Alchemist" → "TA". */
fun monogramOf(name: String): String =
    name.split(' ', '-', '.')
        .mapNotNull { word -> word.firstOrNull { it.isLetterOrDigit() } }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "?" }

/** "5305550101" → "+1 (530) 555-0101". */
fun formatPhone(digits: String): String {
    val local = if (digits.length == 11 && digits.startsWith("1")) digits.substring(1) else digits
    if (local.length != 10) return digits
    return "+1 (${local.substring(0, 3)}) ${local.substring(3, 6)}-${local.substring(6)}"
}

/** "https://www.sierranevada.com/" → "sierranevada.com". */
fun displayHost(url: String): String =
    url.removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .trimEnd('/')

fun formatCoordinates(latitude: Double, longitude: Double): String {
    val lat = String.format(Locale.US, "%.4f° %s", abs(latitude), if (latitude >= 0) "N" else "S")
    val lon = String.format(Locale.US, "%.4f° %s", abs(longitude), if (longitude >= 0) "E" else "W")
    return "$lat, $lon"
}

fun locationLine(city: String, state: String): String =
    listOf(city, state).filter { it.isNotBlank() }.joinToString(", ")
