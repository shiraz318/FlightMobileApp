package com.example.flightmobileapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    lateinit var conncectButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        conncectButton = findViewById(R.id.connect_button)
        conncectButton.setOnClickListener { connect() }
    }

    private fun connect() {
        // Server stuff.

        // If we connected successfully - go to the next activity.
        startActivity(Intent(this, ControlActivity::class.java))
    }
}