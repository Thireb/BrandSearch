package com.closetfinder.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Item::class, ItemFtsEntry::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun itemFtsDao(): ItemFtsDao

    companion object {
        const val FILE_NAME = "closet_finder.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        /** Closes the current database so the underlying file can be replaced (used by restore). */
        fun resetInstance() {
            synchronized(this) {
                instance?.close()
                instance = null
            }
        }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, FILE_NAME)
                // Pre-release app, no user data to preserve across schema changes yet.
                .fallbackToDestructiveMigration()
                .build()
    }
}
