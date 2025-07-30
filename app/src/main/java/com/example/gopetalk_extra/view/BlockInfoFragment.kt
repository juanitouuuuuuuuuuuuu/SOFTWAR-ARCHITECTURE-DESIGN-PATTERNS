package com.example.gopetalk_extra.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gopetalk_extra.R
import com.example.gopetalk_extra.data.api.ApiClient
import com.example.gopetalk_extra.data.api.ChannelService
import com.example.gopetalk_extra.data.storage.SessionManager
import com.example.gopetalk_extra.service.TalkController
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlockInfoFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var channelService: ChannelService

    private lateinit var textChannel: TextView
    private lateinit var textUsers: TextView
    private lateinit var btnDisconnect: MaterialButton

    private var currentChannel: String? = null
    private var isConnected = false

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            currentChannel = sessionManager.getCurrentChannel()
            isConnected = currentChannel != null

            updateUI()


            currentChannel?.let {
                fetchUsersOnChannel(it)
            }

            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.block_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        ApiClient.init(requireContext())
        channelService = ApiClient.getChannelService()

        textChannel = view.findViewById(R.id.text_channel)
        textUsers = view.findViewById(R.id.text_users_on_channel)
        btnDisconnect = view.findViewById(R.id.btn_disconnect)

        btnDisconnect.setOnClickListener {
            TalkController.presenter?.disconnect()
            disconnectFromChannel()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateUI() {
        textChannel.text = currentChannel ?: "N/A"
        btnDisconnect.isEnabled = isConnected

        if (isConnected && currentChannel != null) {
            fetchUsersOnChannel(currentChannel!!)
        } else {
            textUsers.text = "0 users"
        }
    }

    private fun fetchUsersOnChannel(channelName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = channelService.getChannelUsers(channelName)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val users = response.body() ?: emptyList()
                        textUsers.text = "${users.size} conectados "
                    } else {
                        textUsers.text = "Error"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    textUsers.text = "Error"
                }
            }
        }
    }

    private fun disconnectFromChannel() {
        sessionManager.clearCurrentChannel()
        currentChannel = null
        isConnected = false
        updateUI()
    }
}


