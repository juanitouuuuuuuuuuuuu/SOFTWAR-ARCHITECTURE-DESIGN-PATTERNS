package com.example.gopetalk_extra.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gopetalk_extra.R
import com.example.gopetalk_extra.adapter.ChannelsAdapter
import com.example.gopetalk_extra.contract.ChannelContract
import com.example.gopetalk_extra.data.api.ApiClient
import com.example.gopetalk_extra.data.repository.ChannelRepositoryImpl
import com.example.gopetalk_extra.data.storage.SessionManager
import com.example.gopetalk_extra.domain.repository.ChannelRepository
import com.example.gopetalk_extra.presenter.ChannelPresenter

class BlockChannelsFragment : Fragment(), ChannelContract.View {

    private lateinit var presenter: ChannelContract.Presenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.block_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView_channels)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        sessionManager = SessionManager(requireContext())
        ApiClient.init(requireContext())

        val repository: ChannelRepository = ChannelRepositoryImpl(ApiClient.getChannelService())
        presenter = ChannelPresenter(this, repository, sessionManager)

        presenter.getChannels()
    }

    override fun showChannels(channels: List<String>) {
        Log.d("BlockChannelsFragment", "Canales recibidos: $channels")
        recyclerView.adapter = ChannelsAdapter(channels) { selectedChannel ->
            sessionManager.setCurrentChannel(selectedChannel)
            Toast.makeText(requireContext(), "Conectado a: $selectedChannel", Toast.LENGTH_SHORT).show()
        }
    }


    override fun showError(message: String) {
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun navigateToLogin() {
        Toast.makeText(requireContext(), "Sesión cerrada. Redirigiendo...", Toast.LENGTH_SHORT).show()
        requireActivity().finish()
    }

    override fun showLogoutMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}







