package com.example.myapplication.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class LevelActivity : AppCompatActivity(){

    private lateinit var backgroundMusic: BackgroundMusic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level)
        backgroundMusic = BackgroundMusic(this, R.raw.fight_bgm)
        backgroundMusic.start()
    }

    override fun onDestroy() {
        backgroundMusic.stop()
        super.onDestroy()
    }

    fun stopBackgroundMusic() {
        backgroundMusic.stop()
    }
}

