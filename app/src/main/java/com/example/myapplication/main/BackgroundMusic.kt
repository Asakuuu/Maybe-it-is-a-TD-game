package com.example.myapplication.main

import android.content.Context
import android.media.MediaPlayer

class BackgroundMusic(context: Context, musicResId: Int) {
    private var mediaPlayer: MediaPlayer? = null

    init {
        mediaPlayer = MediaPlayer.create(context, musicResId)
    }

    fun start() {
        mediaPlayer?.apply {
            isLooping = true
            start()
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
            mediaPlayer = null
        }
    }
}

