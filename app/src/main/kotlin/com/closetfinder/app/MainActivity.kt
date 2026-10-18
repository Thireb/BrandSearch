package com.closetfinder.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.closetfinder.app.ui.navigation.ClosetFinderNavGraph
import com.closetfinder.app.ui.theme.ClosetFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedImage = SharedImageIntent.from(intent)
        setContent {
            ClosetFinderTheme {
                ClosetFinderNavGraph(sharedImage = sharedImage)
            }
        }
    }
}
