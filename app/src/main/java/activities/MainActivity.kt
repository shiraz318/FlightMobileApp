package activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flightmobileapp.R
import com.google.gson.GsonBuilder
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
import room.RecyclerItemClickListener
import room.URLItem
import room.URLListAdapter
import room.URLViewModel
import java.lang.Exception
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var connectButton: Button
    lateinit var inputUrl: EditText
    private lateinit var urlViewModel: URLViewModel
    private var viewModelJob = Job()
    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    lateinit var retrofit: Retrofit

    // Add another behaviour to onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputUrl = findViewById(R.id.input_text)
        connectButton = findViewById(R.id.connect_button)

        connectButton.setOnClickListener { connect(inputUrl) }
        roomSetting()
    }

    // Initial the Room
    private fun roomSetting() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = URLListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        urlViewModel = ViewModelProvider(this).get(URLViewModel::class.java)
        urlViewModel.allUrls.observe(this, Observer { urls ->
            // Update the cached copy of the words in the adapter.
            urls?.let { adapter.setUrls(it) }
        })
        setOnClickRoom(recyclerView)

    }

    // Operates when row from the list is clicked
    private fun setOnClickRoom(recyclerView: RecyclerView) {
        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(this, recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val url = urlViewModel.getUrlByPosition(position)
//                        if (url == null) {
//                            displayMessage("Error Getting The Required URL")
//                        } else {
                        // Display the given url
                        inputUrl.setText(url)
                        //urlViewModel.initPosition(url)
                        // }
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                    }
                })
        )
    }

    // Connects to the given url address
    private suspend fun connectToServer(url: String) {
        setRetrofit(url)
        try {
            val api = retrofit.create(FlightApiService::class.java)
            val response: Response<ResponseBody> = api.getScreenshotAsync()
            if (response.isSuccessful) {
                //Go to the control view
                val intent = Intent(this@MainActivity, ControlActivity::class.java)
                // Send the given screenshot
                intent.putExtra("Image", response.body()!!.bytes())
                // Send the url
                intent.putExtra("Url", url)
                startActivity(intent)
            } else {
                displayMessage("Connection Failed")
            }
        } catch (e: Exception) {
            if (e.message.toString() == "timeout") {
                displayMessage("Timeout Connecting To The Server. Please Try Again")
            } else {
                displayMessage("Connection Failed")
            }
        }
    }

    // Display toast with a given message
    private fun displayMessage(message: String) {
        val toast = Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.TOP, 0, 210)
        toast.show()
    }

    // Operates when the connect button is clicked
    private fun connect(inputUrl: EditText) {
        // Empty input
        if (TextUtils.isEmpty(inputUrl.text)) {
            displayMessage("URL Input Is Empty. Please Enter URL")
        } else {
            var url = inputUrl.text.toString()
            // Update the url list
            val urlItem = URLItem(url, 0)
            if (urlViewModel.alreadyExists(url) == 1) {
                val position = urlViewModel.getPositionByUrl(url)
                urlViewModel.updatePosition(position)
                urlViewModel.initPosition(url)

            } else {
                urlViewModel.increaseAll()
                urlViewModel.insert(urlItem)
                urlViewModel.deleteExtra()
            }
            // connect to the url address
            // url = "http://10.0.2.2:64673"
            uiScope.launch { connectToServer(url) }

//            // just for debug - delete it.
//            val intent = Intent(this@MainActivity, ControlActivity::class.java)
//            intent.putExtra("Url", url)
//            startActivity(intent)
        }
    }

    // Add another behaviour to onCreate
    override fun onResume() {
        super.onResume()
        inputUrl.setText("")
    }

    // Builds the retrofit
    private fun setRetrofit(url: String) {
        val json = GsonBuilder().setLenient().create()

        val httpClient = OkHttpClient.Builder()
            .callTimeout(12, TimeUnit.SECONDS)
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)

        val builder: Retrofit.Builder = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(json))

        builder.client(httpClient.build())
        retrofit = builder.build()
    }

}


// post.
//
//
// may be not need thread in the manager and use async write and read, and check all the messages before rerutning ok.
// room need to be syncroni and all ui things need to be synchroni including get and post. may be replan runonuiThread to run on new thread.