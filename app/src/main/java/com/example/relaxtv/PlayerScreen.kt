@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.example.relaxtv

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(ambience: Ambience, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var playback by remember { mutableStateOf<AmbientPlayback?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var playing by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }
    var videoPosition by rememberSaveable { mutableLongStateOf(0L) }
    var audioPosition by rememberSaveable { mutableLongStateOf(0L) }
    var shouldPlay by rememberSaveable { mutableStateOf(true) }
    val backFocus = remember { FocusRequester() }
    val goBack by rememberUpdatedState(onBack)

    // Both players share one lifecycle and preserve their own loop positions.
    DisposableEffect(ambience.id, lifecycle) {
        fun release() {
            playback?.let {
                videoPosition = it.videoPosition
                audioPosition = it.audioPosition
                shouldPlay = it.wantsToPlay
                it.release()
            }
            playback = null
            playing = false
            ready = false
        }
        fun start() {
            if (playback != null || error != null) return
            val missing = listOf(ambience.assetPath, ambience.audioAssetPath).firstOrNull { path ->
                runCatching { context.assets.open(path).use { } }.isFailure
            }
            if (missing != null) {
                error = "Falta app/src/main/assets/$missing. Agrega el archivo y vuelve a compilar."
                return
            }
            playback = AmbientPlayback(
                context, ambience, videoPosition, audioPosition, shouldPlay,
                onState = { isPlaying, isReady -> playing = isPlaying; ready = isReady },
                onError = { error = it }
            )
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> start()
                Lifecycle.Event.ON_STOP -> release()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start()
        onDispose { lifecycle.removeObserver(observer); release() }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        val current = playback
        if (error == null && current != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    // Intercept media keys before PlayerView can change only the video.
                    object : PlayerView(ctx) {
                        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                            val code = event.keyCode
                            val toggle = code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER ||
                                code == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || code == KeyEvent.KEYCODE_SPACE ||
                                code == KeyEvent.KEYCODE_NUMPAD_ENTER
                            if (toggle || code == KeyEvent.KEYCODE_MEDIA_PLAY || code == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                                    playback?.let { pair ->
                                        pair.setPlaying(when (code) {
                                            KeyEvent.KEYCODE_MEDIA_PLAY -> true
                                            KeyEvent.KEYCODE_MEDIA_PAUSE -> false
                                            else -> !pair.wantsToPlay
                                        })
                                    }
                                }
                                return true
                            }
                            if (code == KeyEvent.KEYCODE_BACK) {
                                if (event.action == KeyEvent.ACTION_UP) goBack()
                                return true
                            }
                            return super.dispatchKeyEvent(event)
                        }
                    }.apply {
                        useController = false
                        isFocusable = true
                        isFocusableInTouchMode = true
                        player = current.video
                        requestFocus()
                    }
                },
                update = { view ->
                    if (view.player !== current.video) { view.player = current.video; view.requestFocus() }
                },
                onRelease = { it.player = null }
            )
        }
        Column(Modifier.align(Alignment.TopStart).background(Color(0xB3081522)).padding(horizontal = 32.dp, vertical = 12.dp)) {
            Text(ambience.title, color = Color.White, fontSize = 24.sp)
            Text(when { error != null -> "Sin reproducción"; playing -> "Reproduciendo"; ready -> "En pausa"; else -> "Preparando ambiente…" }, color = Color.White, fontSize = 16.sp)
            Text("OK  Pausar / reanudar    •    Back  Volver", color = Accent, fontSize = 14.sp)
        }
        error?.let { message ->
            LaunchedEffect(message) { playback?.setPlaying(false); backFocus.requestFocus() }
            Column(Modifier.align(Alignment.Center).widthIn(max = 620.dp).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ambiente no disponible", color = Color.White, fontSize = 28.sp)
                Spacer(Modifier.height(16.dp))
                Text(message, color = Color(0xFFB7C9D8), fontSize = 18.sp)
                Spacer(Modifier.height(24.dp))
                Text("← Volver al menú", color = Accent, fontSize = 22.sp,
                    modifier = Modifier.focusRequester(backFocus).clickable(onClick = onBack).padding(16.dp))
            }
        }
    }
}
