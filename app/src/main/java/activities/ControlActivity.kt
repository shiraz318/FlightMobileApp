package activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flightmobileapp.Command
import joystick.JoystickData
import joystick.JoystickView
import com.example.flightmobileapp.R
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import network.FlightApiService
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.lang.Thread.sleep
import java.util.*
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
    private lateinit var queue: Queue<String>

    // Add another behaviour to onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        setViews()
        initCommand()
    }

    // Display toast with a given message
    private fun displayMessage(message: String) {
        val element = queue.peek()
        // If this is a new message.
        if (element != message) {
            queue.add(message)
            dequeueMessage()
        }
    }

    // Dequeue a message into the messages queue and display a message.
    private fun dequeueMessage() {
        Thread {
            showMessage()
        }.start()
    }

    // Show a message from the messages queue.
    private fun showMessage() {
        if (!queue.isEmpty()) {
            val message = queue.peek()
            if (message == null) {
                queue.poll()
                return
            }
            runOnUiThread {
                displayToast(message)
            }
            sleep(4000)
            queue.poll()
        }
    }

    // Create a toast and display it with the given message.
    private fun displayToast(message: String) {
        val toast = Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.TOP, 0, 210)
        toast.show()
    }

    // Initials the view components
    private fun setViews() {
        throttleSeekBar = findViewById(R.id.throttle_slider)
        rudderSeekBar = findViewById(R.id.rudder_slider)
        rudderSeekBar.progress = 50
        joystickView = findViewById(R.id.joystickView)
        joystickView.setFunction { onChangeJoystick() }
        initializeSeekBars()
    }

    // Builds the retrofit
    private fun setRetrofit() {
        val url = intent.getStringExtra("Url")
        if (url == null) {
            displayMessage("Error In Url Address")
            return
        }
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

    // Checks if the change is bigger then 1%
    private fun changeEnough(newValue: Float, prevValue: Float, range: Float): Boolean {
        // Get the required number
        val part = calculatePartMove(newValue, prevValue, range)
        // 1% change
        if (part >= 0.01) {
            return true
        }
        return false
    }

    // Calculate the distance it pasted
    private fun calculatePartMove(value: Float, prev: Float, range: Float): Float {
        val difference = abs(value - prev)
        return difference / range

    }

    // Returns a value in a given range
    private fun normalizeValue(value: Float, center: Float, range: Float): Float {
        // Gets the required number
        var part = calculatePartMove(value, center, range)
        if (value < center) {
            // Should be negative
            part *= -1
        }
        return part
    }

    // Checks if the given position is the center of the joystick
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

    // When the joystick moves- check if the movement was significant
    private fun onChangeJoystick() {
        val data = JoystickData(
            joystickView.getElevator(), joystickView.getAileron(),
            joystickView.getOuterRadius(), joystickView.getInnerRadius(), joystickView.getCenterX(),
            joystickView.getCenterY()
        )
        val commandElevator: Float
        val commandAileron: Float
        val range: Float = (data.outerRadius - data.innerRadius) * 2
        var isChangedEnough = false
        // Case of center
        if (checkIfCenter(data.newAileron, data.newElevator, data.centerX, data.centerY)) {
            return
        }
        // Movement should be bigger then 1%
        if (changeEnough(data.newElevator, elevator, range)) {
            isChangedEnough = true
            // The new value
            elevator = data.newElevator
            // number between -1 to 1
            commandElevator = normalizeValue(data.newElevator, data.centerY, range)
        } else {
            // number between -1 to 1
            commandElevator = normalizeValue(elevator, data.centerY, range)
        }
        // Movement should be bigger then 1%
        if (changeEnough(data.newAileron, aileron, range)) {
            isChangedEnough = true
            // The new value
            aileron = data.newAileron
            // number between -1 to 1
            commandAileron = normalizeValue(data.newAileron, data.centerX, range)
        } else {
            // number between -1 to 1
            commandAileron = normalizeValue(aileron, data.centerX, range)
        }
        // Only if was a change
        postIfNeeded(isChangedEnough, commandAileron, commandElevator)
    }

    // Displays the screenshot from the server
    private suspend fun getImage() {
        try {
            val api = retrofit.create(FlightApiService::class.java)
            val response: Response<ResponseBody> = api.getScreenshotAsync()
            if (response.isSuccessful) {
                // Display the given screenshot
                val data = response.body()!!.bytes()
                val b = data.size.let { BitmapFactory.decodeByteArray(data, 0, it) }
                runOnUiThread { screenshot.setImageBitmap(b) }
            } else {
                // Error getting the screenshot
                displayMessage("Could Not Get Screenshot. Please Try Reconnecting")
            }
        } catch (e: Exception) {
            if (e.message.toString() == "timeout") {
                displayMessage("Getting Screenshot Is Taking Too Long. Please Try Reconnecting")
            } else {
                displayMessage("Could Not Get Screenshot. Please Try Reconnecting")
            }
        }
    }

    // Gets the screenshots
    private fun displayImage() {
        Thread {
            while (!stop) {
                uiScope.launch { getImage() }
                sleep(1000)
            }
        }.start()
    }


    // Initial the first command
    private fun initCommand() {
        command = Command(0.0f, 0.0f, 0.0f, 0.0f)
    }

    // Initial the SeekBars
    private fun initializeSeekBars() {
        setThrottleSeekBar()
        setRudderSeekBar()
    }

    // Initial the Throttles SeekBar
    private fun setThrottleSeekBar() {
        throttleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Check the distance it moved
                val difference = calculatePartMove(progress.toFloat(), throttle, 1.0f)
                // In case that the difference is bigger then 1% send a new command
                if (difference >= 1) {
                    command.throttle = progress.toFloat() / 100
                    throttle = progress.toFloat()
                    // Send a new command
                    uiScope.launch { sendCommand() }
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    // Initial the Rudders SeekBar
    private fun setRudderSeekBar() {
        rudderSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Check the distance it moved
                val difference = calculatePartMove(progress.toFloat(), rudder, 1.0f)
                // More then 1%
                if (difference >= 1) {
                    if (progress < 50.0f) {
                        // Negative side
                        command.rudder = (50 - progress).toFloat() / -50
                    } else {
                        // Positive side
                        command.rudder = (progress - 50).toFloat() / 50
                    }
                    // Update to the new value
                    rudder = progress.toFloat()
                    // Send a new command
                    uiScope.launch { sendCommand() }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    // Update command with new values for aileron and elevator
    private fun postIfNeeded(
        isChangedEnough: Boolean,
        commandAileron: Float,
        commandElevator: Float
    ) {
        if (isChangedEnough) {
            // The new values
            command.aileron = commandAileron
            command.elevator = commandElevator
            uiScope.launch { sendCommand() }
        }
    }

    // Sends a command with new values to the server
    private suspend fun sendCommand() {
        try {
            val api = retrofit.create(FlightApiService::class.java)
            val response: Response<Void> = api.postCommand(command)
            // Case of Not Found
            if (response.code() == 404) {
                displayMessage(
                    "Server Connection With The Simulator Encounter Problems." +
                            " Please Try Reconnecting"
                )
                //Case of Error
            } else if (!response.isSuccessful) {
                displayMessage("Could Not Set Values")
            }
        } catch (e: Exception) {
            if (e.message.toString() == "timeout") {
                displayMessage("Setting Values Is Taking Too Long. Please Try Reconnecting")
            } else {
                displayMessage(
                    "Server Connection With The Simulator Encounter Problems." +
                            " Please Try Reconnecting"
                )
            }
        }
    }

    // Add another behaviour to onStop
    override fun onStop() {
        super.onStop()
        // Stop the loop that gets screenshots
        stop = true
        while (!queue.isEmpty()) {
            queue.poll()
        }
    }

    // Add another behaviour to onResume
    override fun onResume() {
        super.onResume()
        // Operates the loop that gets screenshots
        stop = false
        try {
            setRetrofit()
            displayImage()
            queue = LinkedList<String>()
        } catch (e: Exception) {
            displayMessage("Url Is Not Valid. Please Try Reconnecting")
        }
    }

    // Add another behaviour to onStart
    override fun onStart() {
        super.onStart()
        // Display the first screenshot from the Main Activity
        val img = intent.getByteArrayExtra("Image") ?: return
        val b = img.size.let { BitmapFactory.decodeByteArray(img, 0, it) }
        runOnUiThread { screenshot.setImageBitmap(b) }
    }
}
