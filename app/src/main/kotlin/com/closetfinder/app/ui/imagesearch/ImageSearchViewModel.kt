package com.closetfinder.app.ui.imagesearch

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.closetfinder.app.ClosetFinderApp
import com.closetfinder.app.data.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ImageSearchUiState(
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val results: List<Pair<Item, Float>> = emptyList(),
)

class ImageSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ClosetFinderApp).repository

    private val _uiState = MutableStateFlow(ImageSearchUiState())
    val uiState: StateFlow<ImageSearchUiState> = _uiState.asStateFlow()

    fun search(uri: Uri) {
        _uiState.value = ImageSearchUiState(isSearching = true)
        viewModelScope.launch {
            val bitmap = runCatching {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            }.getOrNull()

            val results = if (bitmap != null) {
                val queryEmbedding = repository.embedImage(bitmap)
                bitmap.recycle()
                repository.searchBySimilarity(queryEmbedding)
            } else {
                emptyList()
            }

            _uiState.value = ImageSearchUiState(isSearching = false, hasSearched = true, results = results)
        }
    }
}
