package com.example.gopetalk_extra.data.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.example.gopetalk_extra.data.api.GoWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.ranges.until

class AudioService {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var job: Job? = null

    private val bufferSize = 2048 * 2

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startStreaming(socket: GoWebSocketClient) {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        isRecording = true
        audioRecord?.startRecording()

        job = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (read == bufferSize) {
                    socket.send(buffer)
                } else if (read > 0) {
                    val fixedBuffer = ByteArray(bufferSize)
                    System.arraycopy(buffer, 0, fixedBuffer, 0, read)


                    for (i in read until bufferSize) {
                        fixedBuffer[i] = 0
                    }
                    socket.send(fixedBuffer)
                }
            }
        }
    }

    fun stopStreaming() {
        isRecording = false
        job?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
