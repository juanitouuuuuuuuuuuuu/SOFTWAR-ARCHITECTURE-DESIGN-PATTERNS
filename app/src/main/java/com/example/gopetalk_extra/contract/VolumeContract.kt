package com.example.gopetalk.contract

interface VolumeContract {
    interface View {
        fun showVolume(percent: Int)
        fun showMutedIcon()
        fun showSoundIcon()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun loadVolume()
        fun changeVolume(percent: Int)
    }
}
