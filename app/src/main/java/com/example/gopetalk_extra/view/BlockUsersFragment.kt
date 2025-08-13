package com.example.gopetalk_extra.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gopetalk_extra.R
import com.example.gopetalk_extra.adapter.UserAdapter
import com.example.gopetalk_extra.data.api.ApiClient
import com.example.gopetalk_extra.data.storage.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlockUsersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var sessionManager: SessionManager
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {

        override fun run() {
            checkConnectionAndUpdate()
            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.block_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        recyclerView = view.findViewById(R.id.recyclerView_users)
        adapter = UserAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun checkConnectionAndUpdate() {
        val currentChannel = sessionManager.getCurrentChannel()

        if (currentChannel.isNullOrBlank()) {
            adapter.updateUsers(emptyList())
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getChannelService().getChannelUsers(currentChannel)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val users = response.body() ?: emptyList()
                        adapter.updateUsers(users)
                    } else {
                        adapter.updateUsers(emptyList())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    adapter.updateUsers(emptyList())
                }
            }
        }
    }
}



