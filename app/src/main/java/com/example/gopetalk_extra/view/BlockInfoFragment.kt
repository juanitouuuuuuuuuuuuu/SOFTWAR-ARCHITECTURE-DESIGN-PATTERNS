package com.example.gopetalk_extra.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gopetalk_extra.R
import com.example.gopetalk_extra.adapter.ChannelsAdapter
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
    private lateinit var btnSeeChannels: MaterialButton
    private lateinit var recyclerChannels: RecyclerView

    private var currentChannel: String? = null
    private var isConnected = false

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            currentChannel = sessionManager.getCurrentChannel()
            isConnected = currentChannel != null
            updateUI()
            currentChannel?.let { fetchUsersOnChannel(it) }
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
        btnSeeChannels = view.findViewById(R.id.btn_see_channels)
        recyclerChannels = view.findViewById(R.id.recycler_channels)

        recyclerChannels.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerChannels.visibility = View.GONE

        btnDisconnect.setOnClickListener {
            TalkController.presenter?.disconnect()
            disconnectFromChannel()
        }

        btnSeeChannels.setOnClickListener {
            if (recyclerChannels.visibility == View.GONE) {
                recyclerChannels.visibility = View.VISIBLE
                loadChannels()
            } else {
                recyclerChannels.visibility = View.GONE
            }
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
            textUsers.text = "0 usuarios"
        }
    }

    private fun fetchUsersOnChannel(channelName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = channelService.getChannelUsers(channelName)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val users = response.body() ?: emptyList()
                        textUsers.text = "${users.size} conectados"
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

    private fun loadChannels() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = channelService.getChannels()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val channels = response.body() ?: emptyList()
                        recyclerChannels.adapter = ChannelsAdapter(channels) { selectedChannel ->
                            sessionManager.setCurrentChannel(selectedChannel)
                            currentChannel = selectedChannel
                            updateUI()
                            Toast.makeText(
                                requireContext(),
                                "Conectado a: $selectedChannel",
                                Toast.LENGTH_SHORT
                            ).show()
                            recyclerChannels.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al cargar canales",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
