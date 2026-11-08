package com.example.androidpractices.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

fun Context.openDialer(phoneDigits: String) =
    startSafely(Intent(Intent.ACTION_DIAL, "tel:$phoneDigits".toUri()), "Нет приложения для звонков")

fun Context.openWebsite(url: String) =
    startSafely(Intent(Intent.ACTION_VIEW, url.toUri()), "Нет браузера, чтобы открыть сайт")

fun Context.openMap(latitude: Double, longitude: Double, label: String) {
    val uri = "geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})".toUri()
    startSafely(Intent(Intent.ACTION_VIEW, uri), "Нет приложения с картами")
}

fun Context.openFile(contentUri: String, mimeType: String) {
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(contentUri.toUri(), mimeType)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startSafely(intent, "Нет приложения, чтобы открыть файл")
}

private fun Context.startSafely(intent: Intent, errorMessage: String) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
