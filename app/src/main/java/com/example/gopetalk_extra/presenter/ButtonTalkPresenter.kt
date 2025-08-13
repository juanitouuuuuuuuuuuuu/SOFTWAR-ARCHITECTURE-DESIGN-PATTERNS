package com.example.gopetalk_extra.presenter

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.gopetalk_extra.contract.ButtonTalkContract
import com.example.gopetalk_extra.data.api.ApiClient
import com.example.gopetalk_extra.data.api.AudioPlaybackService
import com.example.gopetalk_extra.data.api.GoWebSocketClient
import com.example.gopetalk_extra.data.audio.AudioService
import com.example.gopetalk_extra.data.audio.GoWebSocketListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ButtonTalkPresenter(
    private val view: ButtonTalkContract.View,
    private val userId: String,
    private val audioService: AudioService,
    private val playbackService: AudioPlaybackService
) : ButtonTalkContract.Presenter {

    private var isTalking = false
    private var isConnected = false
    private var client: GoWebSocketClient? = null
    private var currentChannelName: String = ""
    private var pollingJob: Job? = null

    override fun connectToChannel(channelName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.getChannelService().getChannelUsers(channelName)

            withContext(Dispatchers.Main) {
                if (!channelName.startsWith("canal-")) {
                    view.showError("Canal inválido: $channelName")
                    return@withContext
                }

                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()

                    if (users.size >= 5) {
                        view.showError("El canal está lleno (límite de 5 usuarios)")
                        return@withContext
                    }
                } else {
                    view.showError("No se pudo verificar el canal: ${response.code()}")
                    return@withContext
                }

                disconnect()

                val listener = object : GoWebSocketListener {
                    override fun onAudioMessageReceived(data: ByteArray) {
                        playbackService.play(data)
                    }

                    override fun onTextMessageReceived(message: String) {
                        if (message == "STOP") {
                            stopTalking()
                        }
                    }
                }

                client = GoWebSocketClient(userId, listener)
                client?.connect(channelName)
                isConnected = true
                currentChannelName = channelName

                val channelNumber = Regex("canal-(\\d+)")
                    .find(channelName)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                view.setChannel(channelNumber)
                view.updateStatus("Conectado al $channelName")

                startPollingUserCount(channelName)
            }
        }
    }

    override fun disconnect() {
        if (!isConnected) return

        stopTalking()
        client?.disconnect()
        stopPollingUserCount()
        client = null
        isConnected = false
        view.updateStatus("Desconectado")
    }

    override fun startTalking(receiverId: String) {
        if (!isConnected) {
            view.showError("No está conectado a ningún canal")
            return
        }

        if (isTalking) return
        val context = view.getContextSafe()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            view.showError("Permiso de micrófono denegado")
            return
        }

        val socket = client?.getWebSocket()
        if (socket == null) {
            view.showError("Socket no disponible")
            return
        }

        isTalking = true
        socket.send("START")
        audioService.startStreaming(client!!)
        view.onTalkingStarted()
        Log.d("BlockButtonTalkPresenter", "START enviado")
    }

    override fun stopTalking() {
        if (!isTalking) return
        isTalking = false

        client?.getWebSocket()?.send("STOP")
        audioService.stopStreaming()
        view.onTalkingStopped()
    }

    private fun startPollingUserCount(channelName: String) {
        pollingJob?.cancel()
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isConnected) {
                try {
                    val response = ApiClient.getChannelService().getChannelUsers(channelName)
                    if (response.isSuccessful) {
                        val users = response.body() ?: emptyList()
                        withContext(Dispatchers.Main) {

                        }
                    } else {
                        Log.e("Polling", "Respuesta fallida: ${response.code()}")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        view.showError("Error al obtener usuarios conectados: ${e.message}")
                        Log.e("Polling", "Excepción: ${e.message}")
                    }
                }
                delay(5000)
            }
        }
    }

    private fun stopPollingUserCount() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
