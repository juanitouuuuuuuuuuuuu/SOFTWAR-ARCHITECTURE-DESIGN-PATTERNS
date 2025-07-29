package com.example.gopetalk.domain.repository

interface VolumeRepository {
    fun getVolumePercent(): Int
    fun setVolumePercent(percent: Int)
}
