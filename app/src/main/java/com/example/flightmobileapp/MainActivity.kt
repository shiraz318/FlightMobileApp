package com.example.flightmobileapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var connectButton: Button
    lateinit var inputUrl: EditText
    private lateinit var urlItem: RecyclerView
    lateinit var specificUrl: TextView
    private lateinit var urlViewModel: URLViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputUrl = findViewById(R.id.input_text)
        connectButton = findViewById(R.id.connect_button)
        // urlItem = findViewById(R.id.recyclerview)

        connectButton.setOnClickListener { connect(inputUrl) }
        // urlItem.setOnClickListener { clickMe() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = URLListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        urlViewModel = ViewModelProvider(this).get(URLViewModel::class.java)
        urlViewModel.allUrls.observe(this, Observer { words ->
            // Update the cached copy of the words in the adapter.
            words?.let { adapter.setUrls(it) }
        })
        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListenr(this, recyclerView,
                object : RecyclerItemClickListenr.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        var url = urlViewModel.getUrlByPosition(position)
                        urlViewModel.updatePosition(position)
                        if (url == null) {
                            Toast.makeText(
                                applicationContext,
                                R.string.error_get_url_by_position,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            inputUrl.setText(url)

                            urlViewModel.initPosition(url)
                            Toast.makeText(
                                applicationContext,
                                url,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        Toast.makeText(
                            applicationContext,
                            R.string.clicked,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        )
    }

    private fun connect(inputUrl: EditText) {
        // Server stuff.

        if (TextUtils.isEmpty(inputUrl.text)) {
            Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG
            ).show()
        } else {

            val url = inputUrl.text.toString()
            val word = URLItem(url, 0)
            urlViewModel.increaseAll()
            urlViewModel.insert(word)
            urlViewModel.deleteExtra()
        }

        // If we connected successfully - go to the next activity.
        startActivity(Intent(this, ControlActivity::class.java))
    }
}