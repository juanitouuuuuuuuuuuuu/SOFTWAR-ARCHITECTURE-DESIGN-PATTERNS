package com.example.gopetalk_extra.presenter

import android.util.Log
import com.example.gopetalk_extra.contract.ChannelContract
import com.example.gopetalk_extra.data.api.ApiService
import com.example.gopetalk_extra.data.storage.SessionManager
import com.example.gopetalk_extra.domain.repository.ChannelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChannelPresenter(
    private val view: ChannelContract.View,
    private val repository: ChannelRepository,
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) : ChannelContract.Presenter {

    override fun getChannels() {
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val response = repository.getChannels()


                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    if (response.isSuccessful) {
                        val channels = response.body() ?: emptyList()
                        view.showChannels(channels)
                    } else {
                        view.showError("Error ${response.code()} al obtener los canales")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChannelPresenter", "Error al obtener canales", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error de red: ${e.message}")
                }
            }
        }
    }

    override fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sessionManager.clearSession()
                apiService.logout()
                withContext(Dispatchers.Main) {
                    view.showLogoutMessage("Sesión cerrada correctamente")
                    view.navigateToLogin()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.showError("Error al cerrar sesión")
                }
            }
        }
    }
}
