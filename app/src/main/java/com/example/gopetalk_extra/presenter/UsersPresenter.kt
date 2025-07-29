package com.example.gopetalk_extra.presenter

import com.example.gopetalk_extra.contract.UserContract
import com.example.gopetalk_extra.data.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersPresenter(private val view: UserContract.View) : UserContract.Presenter {

    override fun getUsers(channelName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getChannelService().getChannelUsers(channelName)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val emails = response.body() ?: emptyList()
                        view.showUsers(emails)
                    } else {
                        view.showError("Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.showError("Exception: ${e.message}")
                }
            }
        }
    }
}

