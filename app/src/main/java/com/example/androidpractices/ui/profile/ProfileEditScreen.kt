package com.example.androidpractices.ui.profile

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidpractices.di.ServiceLocator
import kotlinx.coroutines.launch

/**
 * Разрешения в этой практике не запрашиваются (допущено обновлением задания от 10.2026):
 * - галерея открывается через Photo Picker (PickVisualMedia): приложение получает доступ
 *   только к выбранному снимку, READ_MEDIA_IMAGES и READ_EXTERNAL_STORAGE не нужны;
 * - камера вызывается через TakePicture, снимок делает системное приложение и пишет его
 *   в файл из FileProvider. CAMERA в манифесте нет, поэтому запрашивать его не требуется.
 *
 * TODO перед сдачей: указать версии Android, на которых проверены галерея и камера.
 */
@Composable
fun ProfileEditRoute(
    onClose: () -> Unit,
    viewModel: ProfileEditViewModel = viewModel {
        ProfileEditViewModel(ServiceLocator.profileRepository, ServiceLocator.profileFilesRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    // Переживает пересоздание Activity, пока открыто приложение камеры.
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onAvatarSelected(uri.toString())
    }
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val target = pendingCameraUri
        if (success && target != null) viewModel.onAvatarSelected(target)
        pendingCameraUri = null
    }

    LaunchedEffect(state.isDone) {
        if (state.isDone) onClose()
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorShown()
        }
    }

    ProfileEditScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onClose = onClose,
        onFullNameChange = viewModel::onFullNameChange,
        onPositionChange = viewModel::onPositionChange,
        onResumeUrlChange = viewModel::onResumeUrlChange,
        onPickFromGallery = {
            pickImage.launch(
                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onTakePhoto = {
            scope.launch {
                val target = viewModel.createCameraTarget() ?: return@launch
                pendingCameraUri = target
                try {
                    takePicture.launch(target.toUri())
                } catch (e: ActivityNotFoundException) {
                    pendingCameraUri = null
                    Toast.makeText(context, "На устройстве нет приложения камеры", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onDone = viewModel::save,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    state: ProfileEditUiState,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onResumeUrlChange: (String) -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onDone: () -> Unit,
) {
    var showSourceSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = "Закрыть без сохранения")
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
                    onClick = onDone,
                    enabled = state.isLoaded && !state.isSaving && !state.isImportingAvatar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Готово", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (!state.isLoaded) {
            Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EditableAvatar(
                    avatarUri = state.draft.avatarUri,
                    isImporting = state.isImportingAvatar,
                    onClick = { showSourceSheet = true },
                )
                ProfileTextField(
                    value = state.draft.fullName,
                    onValueChange = onFullNameChange,
                    label = "ФИО",
                    icon = Icons.Rounded.Person,
                )
                ProfileTextField(
                    value = state.draft.position,
                    onValueChange = onPositionChange,
                    label = "Должность",
                    icon = Icons.Rounded.Work,
                )
                ProfileTextField(
                    value = state.draft.resumeUrl,
                    onValueChange = onResumeUrlChange,
                    label = "Ссылка на резюме (PDF)",
                    icon = Icons.Rounded.Link,
                    keyboardType = KeyboardType.Uri,
                    errorText = "Укажите корректную ссылку https://…".takeIf { state.isResumeUrlInvalid },
                )
            }
        }
    }

    if (showSourceSheet) {
        ModalBottomSheet(onDismissRequest = { showSourceSheet = false }) {
            AvatarSourceItem(
                title = "Галерея",
                icon = Icons.Rounded.PhotoLibrary,
                onClick = {
                    showSourceSheet = false
                    onPickFromGallery()
                },
            )
            AvatarSourceItem(
                title = "Камера",
                icon = Icons.Rounded.PhotoCamera,
                onClick = {
                    showSourceSheet = false
                    onTakePhoto()
                },
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditableAvatar(
    avatarUri: String?,
    isImporting: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(enabled = !isImporting, onClickLabel = "Выбрать фото", onClick = onClick),
    ) {
        ProfileAvatar(avatarUri = avatarUri)
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        singleLine = true,
        isError = errorText != null,
        supportingText = if (errorText != null) { { Text(errorText) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AvatarSourceItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
