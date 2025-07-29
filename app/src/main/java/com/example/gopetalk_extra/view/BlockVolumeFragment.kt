package com.example.gopetalk_extra.view

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gopetalk_extra.R

class BlockVolumeFragment : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var textVolumen: TextView
    private lateinit var volumeIcon: ImageView
    private lateinit var audioManager: AudioManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.block_volume, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekBar = view.findViewById(R.id.seekBarVolumen)
        textVolumen = view.findViewById(R.id.textVolumen)
        volumeIcon = view.findViewById(R.id.volumeIcon)
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        seekBar.max = maxVolume
        seekBar.progress = currentVolume
        updateVolumeUI(currentVolume, maxVolume)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                updateVolumeUI(progress, maxVolume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateVolumeUI(current: Int, max: Int) {
        val percentage = (current * 100) / max
        textVolumen.text = "$percentage%"

        val iconRes = when {
            percentage == 0 -> R.drawable.without_sound
            percentage in 1..30 -> R.drawable.sound_25_percent
            percentage in 31..70 -> R.drawable.sound_50_percent
            else -> R.drawable.with_sound
        }
        volumeIcon.setImageResource(iconRes)
    }
}


