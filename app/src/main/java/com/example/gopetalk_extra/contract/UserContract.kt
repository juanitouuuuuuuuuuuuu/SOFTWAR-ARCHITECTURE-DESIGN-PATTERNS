package com.example.gopetalk_extra.contract

interface UserContract {

    interface View {
        fun showUsers(emails: List<String>)
        fun showError(message: String)
    }

    interface Presenter {
        fun getUsers(channelName: String)
    }
}
