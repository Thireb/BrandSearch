package com.closetfinder.app.ui.additem

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun AddItemScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    initialUri: Uri? = null,
    viewModel: AddItemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val storeSuggestions by viewModel.storeSuggestions.collectAsState()
    val tagSuggestions by viewModel.tagSuggestions.collectAsState()
    val context = LocalContext.current

    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(viewModel::onImagePicked) }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success -> if (success) pendingCameraUri?.let(viewModel::onImagePicked) }

    LaunchedEffect(initialUri) {
        initialUri?.let(viewModel::onImagePicked)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Surface {
                Button(
                    onClick = { viewModel.save(onSaved) },
                    enabled = state.canSave && !state.isSaving,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Text(if (state.isSaving) "Saving..." else "Save item")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                PhotoPicker(
                    imageUri = state.imageUri,
                    onPick = { showPhotoSourceDialog = true },
                )
            }

            item {
                StoreField(
                    value = state.store,
                    suggestions = storeSuggestions,
                    onValueChange = viewModel::onStoreChanged,
                )
            }

            item {
                OutlinedTextField(
                    value = state.priceText,
                    onValueChange = viewModel::onPriceChanged,
                    label = { Text("Price (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                OutlinedTextField(
                    value = state.salePriceText,
                    onValueChange = viewModel::onSalePriceChanged,
                    label = { Text("Sale price (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                TagsField(
                    pendingTag = state.pendingTag,
                    tags = state.tags,
                    suggestions = tagSuggestions.filter { it !in state.tags },
                    onPendingTagChanged = viewModel::onPendingTagChanged,
                    onCommitTag = viewModel::commitPendingTag,
                    onAddTag = viewModel::addTag,
                    onRemoveTag = viewModel::removeTag,
                )
            }

            item {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChanged,
                    label = { Text("Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Add a photo") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            val uri = createCameraOutputUri(context)
                            pendingCameraUri = uri
                            takePicture.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Take photo", modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Choose from gallery", modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoSourceDialog = false }) { Text("Cancel") }
            },
        )
    }
}

private fun createCameraOutputUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun PhotoPicker(imageUri: Uri?, onPick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Item photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text("Tap to add a photo", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreField(
    value: String,
    suggestions: List<String>,
    onValueChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value, suggestions) {
        if (value.isBlank()) suggestions else suggestions.filter { it.contains(value, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text("Store") },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear store")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            filtered.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsField(
    pendingTag: String,
    tags: List<String>,
    suggestions: List<String>,
    onPendingTagChanged: (String) -> Unit,
    onCommitTag: () -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
) {
    Column {
        OutlinedTextField(
            value = pendingTag,
            onValueChange = onPendingTagChanged,
            label = { Text("Tags") },
            placeholder = { Text("Type a tag and press done") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onCommitTag() }),
            modifier = Modifier.fillMaxWidth(),
        )

        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { onRemoveTag(tag) },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(Icons.Filled.Close, contentDescription = "Remove $tag")
                        },
                    )
                }
            }
        }

        if (suggestions.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                suggestions.take(8).forEach { suggestion ->
                    AssistChip(
                        onClick = { onAddTag(suggestion) },
                        label = { Text(suggestion) },
                    )
                }
            }
        }
    }
}
