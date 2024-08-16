package com.example.myapplication.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class TutorialActivity : AppCompatActivity() {

    private lateinit var backgroundMusic: BackgroundMusic
    private lateinit var tutorialText: TextView
    private var currentStep = 0
    private val tutorialSteps = listOf(
        R.string.tutorial_step1,
        R.string.tutorial_step2,
        R.string.tutorial_step3,
        R.string.tutorial_step4,
        R.string.tutorial_step5
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        tutorialText = findViewById(R.id.tutorialText)

        tutorialText.text = getString(tutorialSteps[currentStep])

        findViewById<View>(android.R.id.content).setOnClickListener {
            currentStep++
            if (currentStep < tutorialSteps.size) {
                tutorialText.text = getString(tutorialSteps[currentStep])
            } else {
                backgroundMusic.stop()
                val intent = Intent(this, LevelActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        backgroundMusic = BackgroundMusic(this,R.raw.main_bgm)
    }

    override fun onDestroy() {
        backgroundMusic.stop()
        super.onDestroy()
    }
}

