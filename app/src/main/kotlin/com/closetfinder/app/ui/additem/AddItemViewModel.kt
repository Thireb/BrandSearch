package com.closetfinder.app.ui.additem

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.closetfinder.app.ClosetFinderApp
import com.closetfinder.app.util.ImageStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddItemUiState(
    val imageUri: Uri? = null,
    val store: String = "",
    val priceText: String = "",
    val salePriceText: String = "",
    val tags: List<String> = emptyList(),
    val pendingTag: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = imageUri != null && priceText.toIntOrNull()?.let { it > 0 } == true
}

class AddItemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ClosetFinderApp).repository

    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()

    val storeSuggestions: StateFlow<List<String>> = repository.observeStores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tagSuggestions: StateFlow<List<String>> = repository.observeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onImagePicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }

    fun onStoreChanged(store: String) {
        _uiState.value = _uiState.value.copy(store = store)
    }

    fun onPriceChanged(text: String) {
        if (text.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(priceText = text)
        }
    }

    fun onSalePriceChanged(text: String) {
        if (text.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(salePriceText = text)
        }
    }

    fun onPendingTagChanged(text: String) {
        _uiState.value = _uiState.value.copy(pendingTag = text)
    }

    fun commitPendingTag() {
        val tag = _uiState.value.pendingTag.trim()
        if (tag.isEmpty()) return
        addTag(tag)
        _uiState.value = _uiState.value.copy(pendingTag = "")
    }

    fun addTag(tag: String) {
        val current = _uiState.value
        if (current.tags.contains(tag)) return
        _uiState.value = current.copy(tags = current.tags + tag)
    }

    fun removeTag(tag: String) {
        val current = _uiState.value
        _uiState.value = current.copy(tags = current.tags - tag)
    }

    fun onNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        val imageUri = state.imageUri ?: return

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            val imagePath = ImageStore.copyToPrivateStorage(getApplication(), imageUri)
            repository.addItem(
                imagePath = imagePath,
                store = state.store.trim(),
                price = state.priceText.toInt(),
                salePrice = state.salePriceText.toIntOrNull(),
                tags = state.tags.joinToString(","),
                notes = state.notes.trim().ifEmpty { null },
            )
            onSaved()
        }
    }
}
