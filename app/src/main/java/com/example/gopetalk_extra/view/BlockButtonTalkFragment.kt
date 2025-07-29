package com.example.gopetalk_extra.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.google.android.material.button.MaterialButton

class BlockButtonTalkFragment : Fragment(), ButtonTalkContract.View {

    private lateinit var presenter: ButtonTalkContract.Presenter
    private lateinit var sessionManager: SessionManager
    private lateinit var btnTalk: MaterialButton

    private val RECORD_AUDIO_REQUEST_CODE = 100
    private var currentChannel = ""

    private val userId: String
        get() = sessionManager.getAccessToken().orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.block_button_talk, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        currentChannel = sessionManager.getCurrentChannel().orEmpty()
        if (currentChannel.isEmpty()) {
            Toast.makeText(requireContext(), "No estás conectado a un canal", Toast.LENGTH_LONG).show()
            return
        }

        presenter.connectToChannel(currentChannel)
        setupTouchEvents()
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
                    presenter.startTalking(userId)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    presenter.stopTalking()
                    true
                }
                else -> false
            }
        }
    }

    override fun onTalkingStarted() {
        activity?.runOnUiThread {
            btnTalk.text = "Micrófono activo"
        }
    }

    override fun onTalkingStopped() {
        activity?.runOnUiThread {
            btnTalk.text = "Silenciado (push to talk)"
        }
    }

    override fun onAudioReceived(data: ByteArray) {
        // Ya lo maneja AudioPlaybackService
    }

    override fun onAudioSent() {}
    override fun showError(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun updateStatus(status: String) {
        // Opcional
    }

    override fun getContextSafe() = requireContext()
    override fun setChannel(channel: Int) {} // Puedes ignorar si no usas int
    override fun getChannel(): Int = 0

    override fun onDestroyView() {
        presenter.disconnect()
        super.onDestroyView()
    }

    override fun setConnectedUsers(users: Int) {
        // Puedes implementar si tienes TextView de usuarios
    }
}
