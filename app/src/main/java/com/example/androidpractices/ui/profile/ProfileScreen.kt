package com.example.androidpractices.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidpractices.di.ServiceLocator
import com.example.androidpractices.ui.common.MessageState
import com.example.androidpractices.ui.common.openFile

@Composable
fun ProfileRoute(
    onEdit: () -> Unit,
    viewModel: ProfileViewModel = viewModel {
        ProfileViewModel(ServiceLocator.profileRepository, ServiceLocator.downloadResume)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.fileToOpen) {
        state.fileToOpen?.let { file ->
            context.openFile(file.contentUri, file.mimeType)
            viewModel.onFileOpened()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorShown()
        }
    }

    ProfileScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEdit = onEdit,
        onResumeClick = viewModel::onResumeClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onEdit: () -> Unit,
    onResumeClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    if (!state.isLoading && !state.profile.isEmpty) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Редактировать профиль")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when {
            state.isLoading -> Box(contentModifier, Alignment.Center) {
                CircularProgressIndicator()
            }

            state.profile.isEmpty -> Box(contentModifier, Alignment.Center) {
                MessageState(
                    icon = Icons.Rounded.Person,
                    title = "Профиль пока пуст",
                    message = "Добавьте фото, имя и ссылку на резюме — данные сохранятся на устройстве",
                    actionLabel = "Заполнить профиль",
                    onAction = onEdit,
                )
            }

            else -> ProfileContent(
                state = state,
                onResumeClick = onResumeClick,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ),
                    shape = MaterialTheme.shapes.large,
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatar(avatarUri = profile.avatarUri)
            Spacer(Modifier.height(16.dp))
            Text(
                text = profile.fullName.ifBlank { "Имя не указано" },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            if (profile.position.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = profile.position,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (profile.hasResume) {
            Button(
                onClick = onResumeClick,
                enabled = !state.isDownloading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (state.isDownloading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Резюме", style = MaterialTheme.typography.titleSmall)
                }
            }
        } else {
            Text(
                text = "Ссылка на резюме не указана",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
