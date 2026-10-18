package com.closetfinder.app.embed

import android.graphics.Bitmap

/** Keeps the embedding model swappable (e.g. for MobileCLIP or DINOv2-small later). */
interface Embedder {
    val modelVersion: Int
    suspend fun embed(bitmap: Bitmap): FloatArray
}
