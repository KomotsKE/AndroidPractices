package com.example.androidpractices.ui.placeholder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chuckerteam.chucker.api.Chucker
import com.example.androidpractices.ui.common.MessageState

/** Экран-заглушка для вкладок, которые появятся в следующих практиках. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    icon: ImageVector,
    headline: String,
    message: String,
    comingIn: String,
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MessageState(icon = icon, title = headline, message = message)
                AssistChip(
                    onClick = {
                        try {
                            context.startActivity(Chucker.getLaunchIntent(context))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    label = { Text("Логи сети (Chucker)") },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }
    }
}
