package com.example.gopetalk.data.repository

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.example.gopetalk.domain.repository.VolumeRepository

class VolumeRepositoryImpl(private val context: Context) : VolumeRepository {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun getVolumePercent(): Int {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return (current * 100) / max
    }

    override fun setVolumePercent(percent: Int) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newVolume = (percent * max) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
    }
}
