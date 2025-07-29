package com.example.gopetalk_extra.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

class AudioPlayer {

    private val sampleRate = 16000
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC,
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize,
        AudioTrack.MODE_STREAM
    ).apply {
        play()
    }

    fun play(data: ByteArray) {
        audioTrack.write(data, 0, data.size)
    }

    fun stop() {
        audioTrack.stop()
        audioTrack.release()
    }
}

