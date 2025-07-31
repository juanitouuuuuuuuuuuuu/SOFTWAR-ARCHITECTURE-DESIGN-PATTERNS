package com.example.gopetalk_extra.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gopetalk_extra.R
import com.example.gopetalk_extra.contract.ButtonTalkContract
import com.example.gopetalk_extra.data.api.AudioPlaybackService
import com.example.gopetalk_extra.data.audio.AudioService
import com.example.gopetalk_extra.data.storage.SessionManager
import com.example.gopetalk_extra.presenter.ButtonTalkPresenter
import com.example.gopetalk_extra.service.TalkController
import com.google.android.material.button.MaterialButton

class BlockButtonTalkFragment : Fragment(), ButtonTalkContract.View {

    private lateinit var presenter: ButtonTalkContract.Presenter
    private lateinit var sessionManager: SessionManager
    private lateinit var btnTalk: MaterialButton
    private val channelCheckHandler = android.os.Handler()
    private lateinit var channelCheckRunnable: Runnable
    private var lastKnownChannel: String? = null
    private val RECORD_AUDIO_REQUEST_CODE = 100
    private var currentChannel = ""
    private lateinit var startSound: MediaPlayer
    private lateinit var endSound: MediaPlayer


    private val userId: String
        get() = sessionManager.getAccessToken().orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.block_button_talk, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSound = MediaPlayer.create(requireContext(), R.raw.audio_start_talking)
        endSound = MediaPlayer.create(requireContext(), R.raw.audio_end_talking)

        btnTalk = view.findViewById(R.id.btn_talk)
        sessionManager = SessionManager(requireContext())

        if (!hasAudioPermission()) {
            requestAudioPermission()
        }

        presenter = ButtonTalkPresenter(
            this,
            userId,
            AudioService(),
            AudioPlaybackService()
        )
        TalkController.presenter = presenter

        currentChannel = sessionManager.getCurrentChannel().orEmpty()

        if (currentChannel.isEmpty()) {
            setDisconnectedButtonStyle()
        } else {
            setConnectedButtonStyle()
            presenter.connectToChannel(currentChannel)
        }

        setupTouchEvents()
        startChannelCheckLoop()
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Permiso concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Permiso de micrófono denegado", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupTouchEvents() {
        btnTalk.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    playStartSound()
                    presenter.startTalking(userId)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    playEndSound()
                    presenter.stopTalking()
                    true
                }
                else -> false
            }
        }
    }

    private fun playStartSound() {
        try {
            if (startSound.isPlaying) {
                startSound.seekTo(0)
            }
            startSound.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playEndSound() {
        try {
            if (endSound.isPlaying) {
                endSound.seekTo(0)
            }
            endSound.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun vibrateShort() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    private fun startChannelCheckLoop() {
        channelCheckRunnable = object : Runnable {
            override fun run() {
                val channel = sessionManager.getCurrentChannel().orEmpty()

                if (channel.isEmpty()) {
                    btnTalk.isEnabled = false
                    btnTalk.text = "Desconectado, selecciona un canal"
                    btnTalk.setIconTintResource(R.color.transparent)
                    btnTalk.elevation = 0f
                    lastKnownChannel = null
                } else {
                    if (lastKnownChannel == null || lastKnownChannel != channel) {
                        presenter.connectToChannel(channel)
                        btnTalk.text = "Pulsa para hablar"
                        btnTalk.setIconResource(R.drawable.micro_inactive)
                        btnTalk.setIconTintResource(R.color.red)
                        btnTalk.elevation = 4f
                    }

                    btnTalk.isEnabled = true
                    lastKnownChannel = channel
                }

                channelCheckHandler.postDelayed(this, 5000)
            }
        }

        channelCheckHandler.post(channelCheckRunnable)
    }
    private fun setDisconnectedButtonStyle() {
        btnTalk.isEnabled = false
        btnTalk.text = "Desconectado"
        btnTalk.setIconTintResource(R.color.transparent)
        btnTalk.elevation = 0f
    }

    private fun setConnectedButtonStyle() {
        btnTalk.isEnabled = true
        btnTalk.text = "Pulsa para hablar"
        btnTalk.setIconResource(R.drawable.micro_inactive)
        btnTalk.setIconTintResource(R.color.red)
        btnTalk.elevation = 4f
    }





    override fun onTalkingStarted() {
        activity?.runOnUiThread {
            btnTalk.text = "Hablando..."
            btnTalk.setIconResource(R.drawable.micro_active)
            btnTalk.setIconTintResource(R.color.green)
            btnTalk.elevation=5f
            vibrateShort()
        }


    }

    override fun onTalkingStopped() {
        activity?.runOnUiThread {
            btnTalk.text = "Silenciado (presiona para hablar)"
            btnTalk.setIconResource(R.drawable.micro_inactive)
            btnTalk.setIconTintResource(R.color.red)
            btnTalk.elevation=50f
        }
    }


    override fun onAudioSent() {}
    override fun showError(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun updateStatus(status: String) {}

    override fun getContextSafe() = requireContext()
    override fun setChannel(channel: Int) {}
    override fun getChannel(): Int = 0

    override fun onDestroyView() {
        presenter.disconnect()
        super.onDestroyView()
        channelCheckHandler.removeCallbacks(channelCheckRunnable)
        startSound.release()
        endSound.release()

    }

}
