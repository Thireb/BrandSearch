package com.closetfinder.app.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.closetfinder.app.ClosetFinderApp
import com.closetfinder.app.data.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ItemDetailUiState(
    val item: Item? = null,
    val isEditing: Boolean = false,
    val storeText: String = "",
    val priceText: String = "",
    val salePriceText: String = "",
    val tagsText: String = "",
    val notesText: String = "",
)

class ItemDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val repository = (application as ClosetFinderApp).repository
    private val itemId: Long = checkNotNull(savedStateHandle["itemId"])

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val item = repository.getById(itemId)
            _uiState.value = ItemDetailUiState(
                item = item,
                storeText = item?.store.orEmpty(),
                priceText = item?.price?.toString().orEmpty(),
                salePriceText = item?.salePrice?.toString().orEmpty(),
                tagsText = item?.tags.orEmpty(),
                notesText = item?.notes.orEmpty(),
            )
        }
    }

    fun startEditing() {
        _uiState.value = _uiState.value.copy(isEditing = true)
    }

    fun cancelEditing() {
        val item = _uiState.value.item
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            storeText = item?.store.orEmpty(),
            priceText = item?.price?.toString().orEmpty(),
            salePriceText = item?.salePrice?.toString().orEmpty(),
            tagsText = item?.tags.orEmpty(),
            notesText = item?.notes.orEmpty(),
        )
    }

    fun onStoreChanged(value: String) {
        _uiState.value = _uiState.value.copy(storeText = value)
    }

    fun onPriceChanged(value: String) {
        if (value.all { it.isDigit() }) _uiState.value = _uiState.value.copy(priceText = value)
    }

    fun onSalePriceChanged(value: String) {
        if (value.all { it.isDigit() }) _uiState.value = _uiState.value.copy(salePriceText = value)
    }

    fun onTagsChanged(value: String) {
        _uiState.value = _uiState.value.copy(tagsText = value)
    }

    fun onNotesChanged(value: String) {
        _uiState.value = _uiState.value.copy(notesText = value)
    }

    fun saveEdits() {
        val state = _uiState.value
        val current = state.item ?: return
        val price = state.priceText.toIntOrNull() ?: return

        val updated = current.copy(
            store = state.storeText.trim(),
            price = price,
            salePrice = state.salePriceText.toIntOrNull(),
            tags = state.tagsText.trim(),
            notes = state.notesText.trim().ifEmpty { null },
        )
        viewModelScope.launch {
            repository.update(updated)
            _uiState.value = state.copy(item = updated, isEditing = false)
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val current = _uiState.value.item ?: return
        viewModelScope.launch {
            repository.delete(current)
            onDeleted()
        }
    }
}
