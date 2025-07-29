package com.example.gopetalk_extra.data.api

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log

class AudioPlaybackService {
    private val bufferSize = AudioTrack.getMinBufferSize(
        16000,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val player = AudioTrack(
        AudioManager.STREAM_MUSIC,
        16000,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize,
        AudioTrack.MODE_STREAM
    )

    fun play(data: ByteArray) {
        if (player.state != AudioTrack.STATE_INITIALIZED) {
            return
        }
        player.play()
        player.write(data, 0, data.size)

    }
}