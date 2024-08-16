package com.example.myapplication.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class GameOverActivity : AppCompatActivity() {

    private lateinit var backgroundMusic: BackgroundMusic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)
        backgroundMusic = BackgroundMusic(this, R.raw.fight_bgm)

        val wave = intent.getIntExtra("wave", 1)

        val waveTextView = findViewById<TextView>(R.id.waveTextView)
        waveTextView.text = "恭喜你撐到第 $wave 波"

        val backToTitleButton = findViewById<Button>(R.id.backToTitleButton)
        backToTitleButton.setOnClickListener {
            backgroundMusic.stop()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    override fun onDestroy() {
        backgroundMusic.stop()
        super.onDestroy()
    }
}

