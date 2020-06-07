package com.example.flightmobileapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class ControlActivity : AppCompatActivity() {
    lateinit var backButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { goBack() }
    }

    private fun goBack() {
        finish()
    }
}