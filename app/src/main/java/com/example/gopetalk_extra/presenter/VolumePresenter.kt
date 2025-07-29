package com.example.gopetalk.presenter

import android.util.Log
import com.example.gopetalk.contract.VolumeContract
import com.example.gopetalk.domain.repository.VolumeRepository

class VolumePresenter(private val repo: VolumeRepository) : VolumeContract.Presenter {

    private var view: VolumeContract.View? = null

    override fun attach(view: VolumeContract.View) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    override fun loadVolume() {
        val percent = repo.getVolumePercent()
        view?.showVolume(percent)
        if (percent == 0) view?.showMutedIcon() else view?.showSoundIcon()
    }

    override fun changeVolume(percent: Int) {
        repo.setVolumePercent(percent)
        view?.showVolume(percent)
        if (percent == 0) view?.showMutedIcon() else view?.showSoundIcon()
    }
}
