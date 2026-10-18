package com.closetfinder.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Long): Item?

    @Query("SELECT * FROM items ORDER BY id DESC LIMIT 1")
    suspend fun getMostRecent(): Item?

    @Query("SELECT DISTINCT store FROM items ORDER BY store")
    fun observeStores(): Flow<List<String>>

    @Query("SELECT tags FROM items")
    fun observeTagsColumn(): Flow<List<String>>

    @Insert
    suspend fun insert(item: Item): Long

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)
}
