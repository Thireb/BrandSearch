package com.closetfinder.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.closetfinder.app.ClosetFinderApp
import com.closetfinder.app.data.Item
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

enum class DatePreset(val label: String, val maxAgeMillis: Long?) {
    ALL_TIME("All time", null),
    TODAY("Today", 24L * 60 * 60 * 1000),
    THIS_WEEK("This week", 7L * 24 * 60 * 60 * 1000),
    THIS_MONTH("This month", 30L * 24 * 60 * 60 * 1000),
}

data class HomeFilters(
    val store: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val datePreset: DatePreset = DatePreset.ALL_TIME,
) {
    val since: Long?
        get() = datePreset.maxAgeMillis?.let { System.currentTimeMillis() - it }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ClosetFinderApp).repository

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _filters = MutableStateFlow(HomeFilters())
    val filters: StateFlow<HomeFilters> = _filters

    val storeOptions: StateFlow<List<String>> = repository.observeStores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val items: StateFlow<List<Item>> = combine(_query.debounce(300), _filters) { q, f -> q to f }
        .flatMapLatest { (q, f) ->
            if (q.isBlank()) {
                repository.observeFiltered(f.store, f.minPrice, f.maxPrice, f.since)
            } else {
                flow { emit(repository.search(q)) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChanged(value: String) {
        _query.value = value
    }

    fun onFiltersChanged(filters: HomeFilters) {
        _filters.value = filters
    }
}
