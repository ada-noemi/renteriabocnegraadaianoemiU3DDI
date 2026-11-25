package com.example.relaxtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.relaxtv.ui.theme.RelaxTVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            RelaxTVTheme {
                var selected by rememberSaveable { mutableStateOf<String?>(null) }
                var lastSelected by rememberSaveable { mutableStateOf("lluvia") }
                val ambience = ambiences.firstOrNull { it.id == selected }
                BackHandler(enabled = ambience != null) { selected = null }
                if (ambience == null) HomeScreen(lastSelected) {
                    lastSelected = it.id
                    selected = it.id
                } else key(ambience.id) { PlayerScreen(ambience) { selected = null } }
            }
        }
    }
}
