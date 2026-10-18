package com.closetfinder.app

import android.app.Application
import com.closetfinder.app.data.AppDatabase
import com.closetfinder.app.data.ItemRepository

class ClosetFinderApp : Application() {

    lateinit var repository: ItemRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = ItemRepository(database.itemDao(), database.itemFtsDao())
    }
}
