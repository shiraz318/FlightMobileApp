package com.example.flightmobileapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import network.FlightApi

lateinit var textView: TextView

class ControlActivity : AppCompatActivity() {

    private var viewModelJob = Job()
    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        textView = findViewById(R.id.response)
    }

    // The internal MutableLiveData String that stores the most recent response
    private val _response = MutableLiveData<String>()

    // The external immutable LiveData for the response String
    val response: LiveData<String>
        get() = _response

    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        getScreenshot()
    }

    /**
     * Sets the value of the status LiveData to the Mars API status.
     */
    private fun getScreenshot() {

        uiScope.launch {
            var deferedReslts = FlightApi.retrofitService.getScreenshotAsync()
            try {
                var item = deferedReslts.await()
               // textView.text = "Success: ${item}"
                Toast.makeText(
                    applicationContext,
                    R.string.app_name,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
               // textView.text = "Failure: ${e.message}"
                Toast.makeText(
                    applicationContext,
                    R.string.error_get_url_by_position,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

//        //_response.value = "Set the Mars API Response here!"
//        FlightApi.retrofitService.getScreenshot()
//        FlightApi.retrofitService.getScreenshot().enqueue(
//            object : Callback<String> {
//
//                override fun onFailure(call: Call<String>, t: Throwable) {
//                    _response.value = "Failure: " + t.message
//                }
//
//                override fun onResponse(
//                    call: Call<String>,
//                    response: Response<String>
//                ) {
//                    _response.value = response.body()
//                    val v: TextView = findViewById(R.id.response)
//                    v.text = _response.value;
//                }
//            })
    }


//    override fun onDraw(canvas: Canvas) {
//        val width = canvas.width
//        val height = canvas.height
//        val centerX = width / 2
//        val centerY = height / 2
//       // canvas.drawCircle()
//    }

}