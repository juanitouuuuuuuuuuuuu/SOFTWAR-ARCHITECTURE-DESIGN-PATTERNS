package com.example.gopetalk_extra.service

import android.util.Log
import com.example.gopetalk_extra.data.storage.SessionManager
import okhttp3.*
import okio.ByteString

object WebSocketManager {

    private const val BASE_URL = "ws://159.203.183.94:8000/ws/talk/"
    private var webSocket: WebSocket? = null
    private var listener: WebSocketListener? = null
    private var onMessageReceived: ((ByteString) -> Unit)? = null
    private lateinit var sessionManager: SessionManager

    fun initialize(session: SessionManager) {
        sessionManager = session
    }

    fun connect(channel: String) {
        if (webSocket != null) return // ya está conectado

        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Log.e("WebSocketManager", "Token no disponible")
            return
        }

        val url = "$BASE_URL$channel/?token=$token"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketManager", "Conectado al canal $channel")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocketManager", "Audio recibido: ${bytes.size} bytes")
                onMessageReceived?.invoke(bytes)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("WebSocketManager", "Cerrando socket: $reason")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketManager", "Fallo de conexión: ${t.message}")
            }
        }

        webSocket = client.newWebSocket(request, listener!!)
    }

    fun send(bytes: ByteString) {
        webSocket?.send(bytes)
    }

    fun disconnect() {
        webSocket?.close(1000, "Cierre solicitado por el usuario")
        webSocket = null
        listener = null
    }

    fun setOnMessageReceived(callback: (ByteString) -> Unit) {
        onMessageReceived = callback
    }
}
