package com.example.gopetalk_extra.contract

import android.content.Context

interface ButtonTalkContract {

    interface View {
        fun onTalkingStarted()
        fun onTalkingStopped()
        fun vibrateShort()
        fun onAudioSent()
        fun showError(message: String)
        fun updateStatus(status: String)
        fun getContextSafe(): Context
        fun setChannel(channel: Int)
        fun getChannel(): Int
        fun setConnectedUsers(users: Int)
    }

    interface Presenter {
        fun connectToChannel(channelName: String)
        fun disconnect()
        fun startTalking(channelName: String)
        fun stopTalking()
    }
}
