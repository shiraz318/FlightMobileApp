package overview
//
//import android.view.View
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.example.flightmobileapp.R
//import network.FlightApi
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
///**
// * The [ViewModel] that is attached to the [OverviewFragment].
// */
//class OverviewViewModel : ViewModel() {
//
//    // The internal MutableLiveData String that stores the most recent response
//    private val _response = MutableLiveData<String>()
//
//    // The external immutable LiveData for the response String
//    val response: LiveData<String>
//        get() = _response
//
//    /**
//     * Call getMarsRealEstateProperties() on init so we can display status immediately.
//     */
//    init {
//        getScreenshot()
//    }
//
//    /**
//     * Sets the value of the status LiveData to the Mars API status.
//     */
//    private fun getScreenshot() {
//        //_response.value = "Set the Mars API Response here!"
//            FlightApi.retrofitService.getScreenshot()
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
//                    val v: View = findViewById(R.id.response)
//                }
//            })
//    }
//}