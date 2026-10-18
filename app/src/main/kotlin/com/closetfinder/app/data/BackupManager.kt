package com.closetfinder.app.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Exports/imports a zip of the database plus item photos, for moving to a new phone. */
object BackupManager {

    suspend fun export(context: Context, destinationUri: Uri) {
        // Flush WAL into the main db file so a single-file copy is consistent.
        AppDatabase.getInstance(context).query("PRAGMA wal_checkpoint(FULL)", null).close()

        val dbFile = context.getDatabasePath(AppDatabase.FILE_NAME)
        val imagesDir = File(context.filesDir, "items")

        context.contentResolver.openOutputStream(destinationUri)?.use { out ->
            ZipOutputStream(out).use { zip ->
                if (dbFile.exists()) addFileToZip(zip, dbFile, AppDatabase.FILE_NAME)
                imagesDir.listFiles()?.forEach { file -> addFileToZip(zip, file, "items/${file.name}") }
            }
        }
    }

    /** Replaces the local database and photos. Caller must restart the app afterwards. */
    suspend fun import(context: Context, sourceUri: Uri) {
        val tempDbFile = File(context.cacheDir, "import_${AppDatabase.FILE_NAME}")
        val imagesDir = File(context.filesDir, "items").apply { mkdirs() }

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    when {
                        entry.name == AppDatabase.FILE_NAME -> copyZipEntry(zip, tempDbFile)
                        entry.name.startsWith("items/") ->
                            copyZipEntry(zip, File(imagesDir, entry.name.removePrefix("items/")))
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        AppDatabase.resetInstance()
        val dbFile = context.getDatabasePath(AppDatabase.FILE_NAME)
        tempDbFile.copyTo(dbFile, overwrite = true)
        tempDbFile.delete()
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File, entryName: String) {
        zip.putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }

    private fun copyZipEntry(zip: ZipInputStream, target: File) {
        target.parentFile?.mkdirs()
        FileOutputStream(target).use { out -> zip.copyTo(out) }
    }
}
