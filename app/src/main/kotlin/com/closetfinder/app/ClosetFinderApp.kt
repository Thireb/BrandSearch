package com.closetfinder.app

import android.app.Application
import com.closetfinder.app.data.AppDatabase
import com.closetfinder.app.data.ItemRepository
import com.closetfinder.app.embed.MediaPipeEmbedder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ClosetFinderApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var repository: ItemRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        val embedder = MediaPipeEmbedder(this)
        repository = ItemRepository(database.itemDao(), database.itemFtsDao(), embedder, applicationScope)
        repository.reembedStaleItemsAsync()
    }
}
