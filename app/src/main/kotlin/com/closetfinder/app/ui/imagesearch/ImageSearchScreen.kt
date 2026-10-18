package com.closetfinder.app.ui.imagesearch

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.closetfinder.app.data.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSearchScreen(
    onBack: () -> Unit,
    onOpenItem: (Long) -> Unit,
    viewModel: ImageSearchViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(viewModel::search) ?: onBack() }

    LaunchedEffect(Unit) {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image search") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isSearching -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                uiState.hasSearched && uiState.results.isEmpty() -> {
                    Text(
                        "No similar items found yet.",
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                uiState.results.isNotEmpty() -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(uiState.results) { index, (item, score) ->
                            ResultRow(rank = index + 1, item = item, score = score) {
                                onOpenItem(item.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(rank: Int, item: Item, score: Float, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("#$rank", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 12.dp))

            AsyncImage(
                model = item.imagePath,
                contentDescription = item.code,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(item.code, style = MaterialTheme.typography.titleMedium)
                Text(item.store, style = MaterialTheme.typography.bodyMedium)
                Text("PKR ${item.salePrice ?: item.price}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${(score * 100).toInt()}% match",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}
