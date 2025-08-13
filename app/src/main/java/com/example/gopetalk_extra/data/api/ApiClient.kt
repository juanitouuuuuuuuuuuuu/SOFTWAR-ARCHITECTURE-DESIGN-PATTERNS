package com.example.gopetalk_extra.data.api

import android.content.Context
import android.util.Log
import com.example.gopetalk_extra.data.storage.SessionManager
import com.example.gopetalk_extra.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private lateinit var retrofit: Retrofit
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var sessionManager: SessionManager

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return

        sessionManager = SessionManager(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        initialized = true
    }

    private fun checkInit() {
        if (!initialized) {
            throw IllegalStateException("ApiClient no inicializado. Llama a ApiClient.init(context) primero.")
        }
    }

    fun getAuthService(): ApiService {
        checkInit()
        return retrofit.create(ApiService::class.java)
    }
    fun getChannelService(): ChannelService {
        checkInit()
        return retrofit.create(ChannelService::class.java)
    }

    fun getWebSocket(channelName: String, userId: String, listener: WebSocketListener): WebSocket {
        checkInit()

        val token = sessionManager.getAccessToken()
        val url = Constants.WS_URL
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        Log.d("WebSocket", "Conectando con header Authorization: Bearer ${token?.take(10)}...")

        return okHttpClient.newWebSocket(request, listener)
    }
    fun getApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }

}


