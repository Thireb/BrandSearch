package com.closetfinder.app.data

import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "items_fts")
data class ItemFtsEntry(
    val itemId: Long,
    val code: String,
    val store: String,
    val tags: String,
    val notes: String,
)
