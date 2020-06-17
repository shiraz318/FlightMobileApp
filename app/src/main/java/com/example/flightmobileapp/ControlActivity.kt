package com.example.flightmobileapp


import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import network.FlightApiService
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class ControlActivity : AppCompatActivity() {

    private var viewModelJob = Job()
    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var aileron: Float = 0.0f
    private var elevator: Float = 0.0f
    private var throttle: Float = 0.0f
    private var rudder: Float = 50.0f
    lateinit var throttleSeekBar: SeekBar
    lateinit var rudderSeekBar: SeekBar
    private lateinit var command: Command
    lateinit var retrofit: Retrofit
    lateinit var joystickView: JoystickView
    private var stop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        setViews()
        initCommand()


//        joystickView.setApplyAnimation {
//            val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
//            joystickView.startAnimation(animation)
//        }

        //sendCommand()


    }

    private fun displayMessage(message: String) {
        if (!stop) {
            val toast = Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.TOP, 0, 210)
            toast.show()
        }
    }

    private fun setViews() {
        throttleSeekBar = findViewById(R.id.throttle_slider)
        rudderSeekBar = findViewById(R.id.rudder_slider)
        rudderSeekBar.progress = 50
        joystickView = findViewById(R.id.joystickView)
        joystickView.setFunction { onChangeJoystick() }
        initializeSeekBars()
    }

    private fun setRetrofit() {
        var url = intent.getStringExtra("Url")
        val json = GsonBuilder().setLenient().create()

        val httpClient = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)

        val builder: Retrofit.Builder = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(json))

        builder.client(httpClient.build())
        retrofit = builder.build()
    }

    private fun changeEnough(newValue: Float, prevValue: Float, range: Float): Boolean {
        val part = calculatePartMove(newValue, prevValue, range)
        if (part >= 0.01) {
            return true
        }
        return false

    }

    private fun calculatePartMove(value: Float, prev: Float, range: Float): Float {

        val difference = abs(value - prev)
        return difference / range

    }

    private fun normalizeValue(value: Float, center: Float, range: Float): Float {
        var part = calculatePartMove(value, center, range)

        if (value < center) {
            part *= -1
        }
        return part
    }

    private fun checkIfCenter(
        newAileron: Float,
        newElevator: Float,
        centerX: Float,
        centerY: Float
    ): Boolean {
        if (newElevator == centerY && newAileron == centerX) {
            elevator = newElevator
            command.elevator = 0.0f
            aileron = newAileron
            command.aileron = 0.0f
            uiScope.launch { sendCommand() }
            return true
        }
        return false
    }

    private fun onChangeJoystick() {

        val newElevator = joystickView.getElevator()
        val newAileron = joystickView.getAileron()
        val outerRadius = joystickView.getOuterRadius()
        val innerRadius = joystickView.getInnerRadius()
        val centerX = joystickView.getCenterX()
        val centerY = joystickView.getCenterY()
        val commandElevator: Float
        val commandAileron: Float
        val range: Float = (outerRadius - innerRadius) * 2
        var isChangedEnough = false

        if (checkIfCenter(newAileron, newElevator, centerX, centerY)) {
            return
        }
        if (changeEnough(newElevator, elevator, range)) {
            isChangedEnough = true
            elevator = newElevator
            commandElevator = normalizeValue(newElevator, centerY, range)
        } else {
            commandElevator = normalizeValue(elevator, centerY, range)
        }

        if (changeEnough(newAileron, aileron, range)) {
            isChangedEnough = true
            aileron = newAileron
            commandAileron = normalizeValue(newAileron, centerX, range)
        } else {
            commandAileron = normalizeValue(aileron, centerX, range)
        }

        if (isChangedEnough) {
            command.aileron = commandAileron
            command.elevator = commandElevator
            uiScope.launch { sendCommand() }
        }
    }

    private suspend fun getImage() {
        try {
            val api = retrofit.create(FlightApiService::class.java)
            val response: Response<ResponseBody> = api.getScreenshotAsync()
            if (response.isSuccessful) {
                val data = response.body()!!.bytes()
                val b = data.size.let { BitmapFactory.decodeByteArray(data, 0, it) }
                runOnUiThread { screenshot.setImageBitmap(b) }
            } else {
                displayMessage("Could Not Get Screenshot. Please Try Reconnecting")
            }
        } catch (e: Exception) {
            displayMessage("Could Not Get Screenshot. Please Try Reconnecting")
        }
//
//
//        api.getScreenshotAsync().enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response1: Response<ResponseBody>) {
//                if (response1.body() == null) {
//                    displayMessage("Could Not Get Screenshot. Please Try Reconnecting")
//                    return
//                }
////                if (response1.isSuccessful) {
//                val data = response1.body()!!.bytes()
//                if (data == null) {
//                    displayMessage("Could Not Get Screenshot. Please Try Reconnecting")
//                } else {
//                    val b = data.size.let { BitmapFactory.decodeByteArray(data, 0, it) }
//                    runOnUiThread { screenshot.setImageBitmap(b) }
//                }
////                } else {
////                    displayMessage("Could Not Get Screenshot. Please Try Reconnecting")
////                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                displayMessage("Could Not Get Screenshot. Please Try Reconnecting")
//            }
//        })
    }

    private fun displayImage() {
        Thread {
            while (!stop) {
                uiScope.launch { getImage() }
                sleep(1000)
            }
        }.start()

    }

    private fun initCommand() {
        command = Command(0.3f, 0.2f, 0.2f, 0.2f)
    }

    private fun initializeSeekBars() {
        throttleSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Write code to perform some action when progress is changed.
                val difference = calculatePartMove(progress.toFloat(), throttle, 1.0f)
                if (difference >= 1) {
                    command.throttle = progress.toFloat() / 100
                    throttle = progress.toFloat()
                    uiScope.launch { sendCommand() }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is stopped.
            }
        })

        rudderSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Write code to perform some action when progress is changed.
                val difference = calculatePartMove(progress.toFloat(), rudder, 1.0f)
                if (difference >= 1) {
                    if (progress < 50.0f) {
                        command.rudder = (50 - progress).toFloat() / -50
                    } else {
                        command.rudder = (progress - 50).toFloat() / 50
                    }
                    rudder = progress.toFloat()
                    uiScope.launch { sendCommand() }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is stopped.
                //Toast.makeText(this@ControlActivity, "Progress is " + seekBar.progress + "%", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private suspend fun sendCommand() {

        Log.d("TAG", "inpost")

        try {
            val api = retrofit.create(FlightApiService::class.java)
            val response: Response<Void> = api.postCommand(command)
            if (response.isSuccessful) {
                Log.d("TAG", "success")
                Log.d("TAG", response.message())
            } else if (response.code() == 404) {
                displayMessage("Oops! Something Is Wrong. Please Try Reconnecting")
            } else {
                displayMessage("Could Not Set Values")
            }
        } catch (e: Exception) {
            displayMessage("Could Not Get Screenshot. Please Try Reconnecting")
        }

//        Log.d("TAG", "inpost")
//
//        val api = retrofit.create(FlightApiService::class.java)
//        api.postCommand(command).enqueue(object : Callback<Void> {
//            override fun onResponse(
//                call: Call<Void>,
//                response: Response<Void>
//            ) {
//                if (response.isSuccessful) {
//                    Log.d("TAG", "success")
//                    Log.d("TAG", response.message())
//                } else if (response.code() == 404) {
//                    displayMessage("Oops! Something Is Wrong. Please Try Reconnecting")
//                } else {
//                    displayMessage("Could Not Set Values")
//                }
//            }
//
//            override fun onFailure(call: Call<Void>, t: Throwable) {
//                displayMessage("Oops! Something Is Wrong. Please Try Reconnecting")
//            }
//        })
    }

    override fun onStop() {
        super.onStop()
        stop = true
    }

    override fun onPause() {
        super.onPause()
        stop = true
    }

    override fun onResume() {
        super.onResume()
        stop = false
        setRetrofit()
        displayImage()
    }

}
