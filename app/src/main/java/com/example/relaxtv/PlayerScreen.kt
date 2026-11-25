@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text

@Composable
fun PlayerScreen(ambience: Ambience, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var position by rememberSaveable { mutableLongStateOf(0L) }
    var shouldPlay by rememberSaveable { mutableStateOf(true) }
    val backFocus = remember { FocusRequester() }

    // Release on STOP and when leaving; retain position and the user's pause state.
    DisposableEffect(ambience.id, lifecycle) {
        fun release() {
            player?.let {
                position = it.currentPosition
                shouldPlay = it.playWhenReady
                it.release()
            }
            player = null
        }
        fun start() {
            if (player != null || error != null) return
            if (runCatching { context.assets.open(ambience.assetPath).use { } }.isFailure) {
                error = "Falta ${ambience.id}.mp4. Colócalo en app/src/main/assets/videos/ y vuelve a compilar."
                return
            }
            player = ExoPlayer.Builder(context).build().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
                setHandleAudioBecomingNoisy(true)
                repeatMode = Player.REPEAT_MODE_ONE
                addListener(object : Player.Listener {
                    override fun onPlayerError(exception: PlaybackException) {
                        error = "No se pudo reproducir ${ambience.id}.mp4 (${exception.errorCodeName}). Verifica que tenga video y audio compatibles."
                    }
                })
                setMediaItem(MediaItem.fromUri("asset:///${ambience.assetPath}"))
                seekTo(position)
                playWhenReady = shouldPlay
                prepare()
            }
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
        val current = player
        if (error == null && current != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx -> PlayerView(ctx).apply {
                    useController = true
                    controllerShowTimeoutMs = 3500
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setShowFastForwardButton(false)
                    setShowRewindButton(false)
                    setOnKeyListener { _, code, event ->
                        val toggle = code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || code == KeyEvent.KEYCODE_SPACE
                        when {
                            toggle -> {
                                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                                    this.player?.let { if (it.playWhenReady) it.pause() else it.play() }
                                    showController()
                                }
                                true
                            }
                            code == KeyEvent.KEYCODE_BACK -> {
                                if (event.action == KeyEvent.ACTION_UP) onBack()
                                true
                            }
                            else -> false
                        }
                    }
                    isFocusable = true
                    isFocusableInTouchMode = true
                    this.player = current
                    requestFocus()
                } },
                update = { view ->
                    if (view.player !== current) { view.player = current; view.requestFocus() }
                },
                onRelease = { it.player = null }
            )
        }
        Column(Modifier.align(Alignment.TopStart).background(Color(0xB3081522)).padding(horizontal = 32.dp, vertical = 12.dp)) {
            Text(ambience.title, color = Color.White, fontSize = 24.sp)
            Text("OK  Pausar / reanudar    •    Back  Volver", color = Accent, fontSize = 14.sp)
        }
        error?.let { message ->
            LaunchedEffect(message) { player?.pause(); backFocus.requestFocus() }
            Column(Modifier.align(Alignment.Center).widthIn(max = 620.dp).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Video no disponible", color = Color.White, fontSize = 28.sp)
                Spacer(Modifier.height(16.dp))
                Text(message, color = Color(0xFFB7C9D8), fontSize = 18.sp)
                Spacer(Modifier.height(24.dp))
                Text("← Volver al menú", color = Accent, fontSize = 22.sp,
                    modifier = Modifier.focusRequester(backFocus).clickable(onClick = onBack).padding(16.dp))
            }
        }
    }
}
