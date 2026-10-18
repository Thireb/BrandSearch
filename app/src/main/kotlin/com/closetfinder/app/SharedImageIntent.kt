package com.closetfinder.app

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

data class SharedImageIntent(val uri: Uri, val mode: Mode) {
    enum class Mode { ADD_ITEM, FIND_ITEM }

    companion object {
        private const val ALIAS_FIND_ITEM = "com.closetfinder.app.ShareFindItem"

        fun from(intent: Intent?): SharedImageIntent? {
            if (intent?.action != Intent.ACTION_SEND) return null
            if (intent.type?.startsWith("image/") != true) return null
            val uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java) ?: return null

            val mode = if (intent.component?.className == ALIAS_FIND_ITEM) Mode.FIND_ITEM else Mode.ADD_ITEM
            return SharedImageIntent(uri, mode)
        }
    }
}
