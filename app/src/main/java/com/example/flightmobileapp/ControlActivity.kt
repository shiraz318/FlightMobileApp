package com.example.flightmobileapp


import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import network.FlightApiService
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ControlActivity : AppCompatActivity() {

    private var viewModelJob = Job()
    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
     private var lastThrottle:Float = 0.0f
    private var lastRudder:Float = 0.0f
    private var lastAileron:Float = 0.0f
    private var lastElevator:Float = 0.0f
    lateinit var throttleSeekBar: SeekBar
    lateinit var rudderSeekBar: SeekBar
    private lateinit var command: Command
    lateinit var retrofit: Retrofit
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        throttleSeekBar = findViewById(R.id.throttle_slider)
        rudderSeekBar = findViewById(R.id.rudder_slider)
        initCommand()
        var url = intent.getStringExtra("Url")
        val gson = GsonBuilder().setLenient().create()
//        retrofit = Retrofit.Builder().baseUrl(url)
//            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        val httpClient = OkHttpClient.Builder()
            .callTimeout(2, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)

        val builder: Retrofit.Builder = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))

        builder.client(httpClient.build())

        retrofit = builder.build()
        sendCommand()


            //  setValues()

      //  initializeSeekBars()

//        var bytes: ByteArray? = intent.getByteArrayExtra("ResponseImage")
//        imageFromServer(bytes)
    }

    private fun initCommand() {
        command = Command(0.3f, 0.2f, 0.2f, 0.2f)
    }

//    private fun setValues() {
//        lastAileron
//        lastRudder = rudderSeekBar.progress.toFloat()
//        lastThrottle = throttleSeekBar.progress.toFloat()
//        throttleSeekBar.
//
//    }
//    private fun initializeSeekBars() {
//        throttleSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                // Write code to perform some action when progress is changed.
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//                // Write code to perform some action when touch is started.
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                // Write code to perform some action when touch is stopped.
//                Toast.makeText(this@ControlActivity, "Progress is " + seekBar.progress + "%", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        rudderSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                // Write code to perform some action when progress is changed.
//
//                var length = seekBar.max
//                var realValue = progress / length
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//                // Write code to perform some action when touch is started.
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                // Write code to perform some action when touch is stopped.
//                Toast.makeText(this@ControlActivity, "Progress is " + seekBar.progress + "%", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
    private fun imageFromServer(data: ByteArray?) {
        // val i= response?.body()?.byteStream()
        val b = data?.size?.let { BitmapFactory.decodeByteArray(data, 0, it) };
        // val b = BitmapFactory.decodeStream(i)
        runOnUiThread { screenshot.setImageBitmap(b) }
    }

    private fun sendCommand() {
        val api = retrofit.create(FlightApiService::class.java)
        api.postCommand(command).enqueue(object : Callback<Void> {
            override fun onResponse(
                call: Call<Void>,
                response: Response<Void>
            ) {
                if (!response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        response.errorBody()!!.string(),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    t.message + " fail",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

}
