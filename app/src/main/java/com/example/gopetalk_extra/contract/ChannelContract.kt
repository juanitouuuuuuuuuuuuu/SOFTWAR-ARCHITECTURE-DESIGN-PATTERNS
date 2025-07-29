package com.example.gopetalk_extra.contract

interface ChannelContract {

    interface View {
        fun showChannels(channels: List<String>)
        fun showError(message: String)
        fun showLoading()
        fun hideLoading()
        fun navigateToLogin()
        fun showLogoutMessage(message: String)
    }

    interface Presenter {
        fun getChannels()
        fun logout()
    }
}

