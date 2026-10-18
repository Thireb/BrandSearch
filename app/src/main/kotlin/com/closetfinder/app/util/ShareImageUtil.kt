package com.closetfinder.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.closetfinder.app.data.Item
import java.io.File
import java.io.FileOutputStream

/** Stamps the item code in a corner, legible on light or dark photos, then opens the share sheet. */
object ShareImageUtil {

    fun shareStampedImage(context: Context, item: Item) {
        val source = BitmapFactory.decodeFile(item.imagePath) ?: return
        val stamped = stampCode(source, item.code)
        source.recycle()

        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "${item.code}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { stamped.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        stamped.recycle()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share ${item.code}"))
    }

    private fun stampCode(source: Bitmap, code: String): Bitmap {
        val bitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)

        val textSizePx = bitmap.width * 0.06f
        val padding = textSizePx * 0.5f
        val label = "#$code"

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = textSizePx
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textWidth = textPaint.measureText(label)

        val boxRight = bitmap.width - padding
        val boxBottom = bitmap.height - padding
        val boxLeft = boxRight - textWidth - padding * 2
        val boxTop = boxBottom - textSizePx - padding

        val backgroundPaint = Paint().apply { color = Color.argb(180, 0, 0, 0) }
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxBottom), padding, padding, backgroundPaint)
        canvas.drawText(label, boxLeft + padding, boxBottom - padding, textPaint)

        return bitmap
    }
}
