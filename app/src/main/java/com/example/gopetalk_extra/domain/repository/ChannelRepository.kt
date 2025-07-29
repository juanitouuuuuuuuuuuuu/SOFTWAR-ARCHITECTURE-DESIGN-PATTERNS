package com.example.gopetalk_extra.domain.repository

import retrofit2.Response

interface ChannelRepository {
    suspend fun getChannels(): Response<List<String>>
}

