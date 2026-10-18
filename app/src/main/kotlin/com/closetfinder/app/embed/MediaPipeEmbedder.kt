package com.closetfinder.app.embed

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaPipeEmbedder(context: Context) : Embedder {

    override val modelVersion = MODEL_VERSION

    private val delegate: ImageEmbedder = ImageEmbedder.createFromOptions(
        context,
        ImageEmbedder.ImageEmbedderOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath(MODEL_ASSET).build())
            .setL2Normalize(true)
            .build(),
    )

    override suspend fun embed(bitmap: Bitmap): FloatArray = withContext(Dispatchers.Default) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result = delegate.embed(mpImage)
        result.embeddingResult().embeddings()[0].floatEmbedding()
    }

    companion object {
        const val MODEL_VERSION = 1
        private const val MODEL_ASSET = "mobilenet_v3_small.tflite"
    }
}
