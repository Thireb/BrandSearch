package com.closetfinder.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItemFtsDao {

    @Insert
    suspend fun insert(entry: ItemFtsEntry)

    @Query("DELETE FROM items_fts WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: Long)

    @Query("SELECT itemId FROM items_fts WHERE items_fts MATCH :query")
    suspend fun search(query: String): List<Long>
}
