package com.example.flightmobileapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    lateinit var connectButton: Button
    lateinit var inputUrl: EditText
    //lateinit var urlItem: TextView
    private lateinit var urlViewModel: URLViewModel

   // @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputUrl = findViewById(R.id.input_text)
        connectButton = findViewById(R.id.connect_button)
        connectButton.setOnClickListener { connect(inputUrl) }
//        urlItem = findViewById(R.id.textView)
//        urlItem.setOnClickListener { changUrl(inputUrl, urlItem) }
//
//
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = URLListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        urlViewModel = ViewModelProvider(this).get(URLViewModel::class.java)
        urlViewModel.allUrls.observe(this, Observer { words ->
            // Update the cached copy of the words in the adapter.
            words?.let { adapter.setUrls(it) }
        })

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Toast.makeText(
//            applicationContext,
//            R.string.inside,
//            Toast.LENGTH_LONG
//        ).show()
//        if (requestCode == newURLActivityRequestCode && resultCode == Activity.RESULT_OK) {
//            Toast.makeText(
//                applicationContext,
//                R.string.inside,
//                Toast.LENGTH_LONG
//            ).show()
//            data?.getStringExtra(MainActivity.EXTRA_REPLY)?.let {
//                val word = URL(it, "1")
//                urlViewModel.insert(word)
//            }
//        } else {
//            Toast.makeText(
//                applicationContext,
//                R.string.empty_not_saved,
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }

//    companion object {
//        const val EXTRA_REPLY = "com.example.android.urllistsql.REPLY"
//    }

  //  @RequiresApi(Build.VERSION_CODES.O)
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
            val word = URL(url, "1")
            urlViewModel.insert(word)
        }

        // If we connected successfully - go to the next activity.
        //startActivity(Intent(this, ControlActivity::class.java))
    }

    //private fun changUrl(inputUrl: EditText, url: TextView) {
       // inputUrl.text = url.text as Editable?

    //}

}