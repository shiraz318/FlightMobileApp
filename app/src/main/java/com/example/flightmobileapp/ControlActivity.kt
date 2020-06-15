package com.example.flightmobileapp


import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class ControlActivity : AppCompatActivity() {

    private var viewModelJob = Job()
    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var aileron: Float = 0.0f
    private var elevator: Float = 0.0f
    private var throttle: Float = 50.0f
    private var rudder: Float = 0.0f
    lateinit var throttleSeekBar: SeekBar
    lateinit var rudderSeekBar: SeekBar
    private lateinit var command: Command
    lateinit var retrofit: Retrofit
    lateinit var joystickView: JoystickView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        throttleSeekBar = findViewById(R.id.throttle_slider)
        throttleSeekBar.progress = 50
        rudderSeekBar = findViewById(R.id.rudder_slider)
        joystickView = findViewById(R.id.joystickView)
        joystickView.setFunction { onChangeJoystick() }
        initCommand()
        var url = intent.getStringExtra("Url")
        val gson = GsonBuilder().setLenient().create()

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
        initializeSeekBars()
        //sendCommand()


        //  setValues()

        //  initializeSeekBars()
//
//        var bytes: ByteArray? = intent.getByteArrayExtra("ResponseImage")
//        imageFromServer(bytes)

        displayImage()
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
            sendCommand()
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
            sendCommand()
        }
    }

    private fun getImage() {
        val api = retrofit.create(FlightApiService::class.java)

        api.getScreenshotAsync().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response1: Response<ResponseBody>) {
                val data = response1.body()!!.bytes()
                val b = data?.size?.let { BitmapFactory.decodeByteArray(data, 0, it) }
                runOnUiThread { screenshot.setImageBitmap(b) }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    private fun displayImage() {
        Thread {
            while (true) {
                getImage()
                sleep(1000)
            }
        }.start()

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
    private fun initializeSeekBars() {
        rudderSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Write code to perform some action when progress is changed.
                val difference = calculatePartMove(progress.toFloat(), rudder, 1.0f)
                if (difference >= 1) {
                    command.rudder = progress.toFloat() / 100
                    rudder = progress.toFloat()
                    sendCommand()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is stopped.
            }
        })

        throttleSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Write code to perform some action when progress is changed.
                val difference = calculatePartMove(progress.toFloat(), throttle, 1.0f)
                if (difference >= 1) {
                    if (progress < 50.0f) {
                        command.throttle = (50 - progress).toFloat() / -50
                    } else {
                        command.throttle = (progress - 50).toFloat() / 50
                    }
                    throttle = progress.toFloat()
                    sendCommand()
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

    override fun onDestroy() {
        super.onDestroy()
        val api = retrofit.create(FlightApiService::class.java)
        api.disconnect().enqueue(object : Callback<Void> {
            override fun onResponse(
                call: Call<Void>,
                response: Response<Void>
            ) {
                Toast.makeText(
                    applicationContext,
                    " disconnected",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    " disconnected failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
