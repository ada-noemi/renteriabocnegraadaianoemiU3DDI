package com.example.relaxtv

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/** Owns the video/audio pair. All calls and listener events run on the main thread. */
internal class AmbientPlayback(
    context: Context,
    ambience: Ambience,
    initialVideoPosition: Long,
    initialAudioPosition: Long,
    initiallyPlaying: Boolean,
    private val onState: (playing: Boolean, ready: Boolean) -> Unit,
    private val onError: (String) -> Unit,
) {
    val video = ExoPlayer.Builder(context).build()
    private val audio = ExoPlayer.Builder(context).build()
    var wantsToPlay = initiallyPlaying
        private set
    val videoPosition get() = video.currentPosition
    val audioPosition get() = audio.currentPosition
    private var released = false
    private var failed = false
    private var updating = false

    init {
        // Only the audio player requests focus; mute any embedded video soundtrack.
        video.volume = 0f
        audio.setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true
        )
        audio.setHandleAudioBecomingNoisy(true)
        listOf(video to ambience.assetPath, audio to ambience.audioAssetPath).forEach { (player, path) ->
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) = synchronize()

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    if (player === audio && !playWhenReady &&
                        (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS ||
                            reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY)) {
                        setPlaying(false)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    if (released || failed) return
                    failed = true
                    wantsToPlay = false
                    video.pause()
                    audio.pause()
                    onState(false, false)
                    onError("No se pudo reproducir $path (${error.errorCodeName}). Verifica el archivo local.")
                }
            })
            player.setMediaItem(MediaItem.fromUri("asset:///$path"))
        }
        video.seekTo(initialVideoPosition)
        audio.seekTo(initialAudioPosition)
        video.prepare()
        audio.prepare()
    }

    fun setPlaying(play: Boolean) {
        if (released || failed) return
        wantsToPlay = play
        synchronize()
    }

    private fun synchronize() {
        if (released || failed || updating) return
        updating = true
        try {
            val ready = video.playbackState == Player.STATE_READY && audio.playbackState == Player.STATE_READY
            val play = wantsToPlay && ready
            audio.playWhenReady = play
            video.playWhenReady = play && audio.playbackSuppressionReason == Player.PLAYBACK_SUPPRESSION_REASON_NONE
            onState(video.isPlaying && audio.isPlaying, ready)
        } finally {
            updating = false
        }
    }

    fun release() {
        if (released) return
        released = true
        video.release()
        audio.release()
    }
}
