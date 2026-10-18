package com.closetfinder.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.closetfinder.app.data.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddItem: () -> Unit,
    onOpenItem: (Long) -> Unit,
    onImageSearch: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val items by viewModel.items.collectAsState()
    val query by viewModel.query.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val storeOptions by viewModel.storeOptions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Closet Finder") },
                actions = {
                    IconButton(onClick = onImageSearch) {
                        Icon(Icons.Filled.ImageSearch, contentDescription = "Search by image")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) {
                Icon(Icons.Filled.Add, contentDescription = "Add item")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search code, store, tag, note...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
            )

            FilterRow(
                filters = filters,
                storeOptions = storeOptions,
                onFiltersChanged = viewModel::onFiltersChanged,
            )

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val message = if (query.isBlank() && filters == HomeFilters()) {
                        "No items yet. Tap + to add your first item."
                    } else {
                        "No items match."
                    }
                    Text(message, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        ItemCard(item = item, onClick = { onOpenItem(item.id) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    filters: HomeFilters,
    storeOptions: List<String>,
    onFiltersChanged: (HomeFilters) -> Unit,
) {
    var storeMenuOpen by remember { mutableStateOf(false) }
    var dateMenuOpen by remember { mutableStateOf(false) }
    var priceDialogOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box {
            FilterChip(
                selected = filters.store != null,
                onClick = { storeMenuOpen = true },
                label = { Text(filters.store ?: "Store") },
            )
            DropdownMenu(expanded = storeMenuOpen, onDismissRequest = { storeMenuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("All stores") },
                    onClick = {
                        onFiltersChanged(filters.copy(store = null))
                        storeMenuOpen = false
                    },
                )
                storeOptions.forEach { store ->
                    DropdownMenuItem(
                        text = { Text(store) },
                        onClick = {
                            onFiltersChanged(filters.copy(store = store))
                            storeMenuOpen = false
                        },
                    )
                }
            }
        }

        FilterChip(
            selected = filters.minPrice != null || filters.maxPrice != null,
            onClick = { priceDialogOpen = true },
            label = {
                val label = if (filters.minPrice != null || filters.maxPrice != null) {
                    "PKR ${filters.minPrice ?: 0}-${filters.maxPrice ?: "∞"}"
                } else {
                    "Price"
                }
                Text(label)
            },
        )

        Box {
            FilterChip(
                selected = filters.datePreset != DatePreset.ALL_TIME,
                onClick = { dateMenuOpen = true },
                label = { Text(filters.datePreset.label) },
            )
            DropdownMenu(expanded = dateMenuOpen, onDismissRequest = { dateMenuOpen = false }) {
                DatePreset.entries.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.label) },
                        onClick = {
                            onFiltersChanged(filters.copy(datePreset = preset))
                            dateMenuOpen = false
                        },
                    )
                }
            }
        }
    }

    if (priceDialogOpen) {
        var minText by remember { mutableStateOf(filters.minPrice?.toString().orEmpty()) }
        var maxText by remember { mutableStateOf(filters.maxPrice?.toString().orEmpty()) }

        AlertDialog(
            onDismissRequest = { priceDialogOpen = false },
            title = { Text("Price range (PKR)") },
            text = {
                Column {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { if (it.all(Char::isDigit)) minText = it },
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = { if (it.all(Char::isDigit)) maxText = it },
                        label = { Text("Max") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onFiltersChanged(
                        filters.copy(minPrice = minText.toIntOrNull(), maxPrice = maxText.toIntOrNull()),
                    )
                    priceDialogOpen = false
                }) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onFiltersChanged(filters.copy(minPrice = null, maxPrice = null))
                    priceDialogOpen = false
                }) { Text("Clear") }
            },
        )
    }
}

@Composable
private fun ItemCard(item: Item, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Box {
            AsyncImage(
                model = item.imagePath,
                contentDescription = item.code,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    item.code,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Text(
            item.store,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp),
        )
        val priceText = if (item.salePrice != null) {
            "PKR ${item.salePrice} (was ${item.price})"
        } else {
            "PKR ${item.price}"
        }
        Text(priceText, style = MaterialTheme.typography.bodyMedium)
    }
}
