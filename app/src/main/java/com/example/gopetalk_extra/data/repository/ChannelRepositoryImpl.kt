package com.example.gopetalk_extra.data.repository

import com.example.gopetalk_extra.data.api.ChannelService
import com.example.gopetalk_extra.domain.repository.ChannelRepository
import retrofit2.Response

class ChannelRepositoryImpl(
    private val api: ChannelService
) : ChannelRepository {

    override suspend fun getChannels(): Response<List<String>> {
        return try {
            api.getChannels()
        } catch (e: Exception) {
            Response.error(
                500,
                okhttp3.ResponseBody.create(null, e.message ?: "Error desconocido")
            )
        }
    }
}





