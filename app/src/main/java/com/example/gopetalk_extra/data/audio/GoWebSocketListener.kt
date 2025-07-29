package com.example.gopetalk_extra.data.audio

interface GoWebSocketListener {
    fun onAudioMessageReceived(data: ByteArray)
    fun onTextMessageReceived(message: String)
}