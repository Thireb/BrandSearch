package com.closetfinder.app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/** Copies a picked image into app-private storage so it survives the source Uri's lifetime. */
object ImageStore {

    fun copyToPrivateStorage(context: Context, source: Uri): String {
        val imagesDir = File(context.filesDir, "items").apply { mkdirs() }
        val destination = File(imagesDir, "${UUID.randomUUID()}.jpg")

        context.contentResolver.openInputStream(source)?.use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return destination.absolutePath
    }
}
