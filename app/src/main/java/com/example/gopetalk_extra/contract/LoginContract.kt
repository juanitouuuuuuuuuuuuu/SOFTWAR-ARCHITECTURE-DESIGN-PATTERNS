package com.example.gopetalk_extra.contract

interface LoginContract {
    interface View {
        fun showLoginSuccess()
        fun showLoginError(message: String)
        fun navigateToHome()
    }

    interface Presenter {
        fun login(email: String, password: String)
        fun checkSession()
    }
}