package network

import com.example.flightmobileapp.Command
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

//private var moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//
//private const val BASE_URL =
//    "http://10.0.2.2:64673/"
//
//private val retrofit = Retrofit.Builder()
//    .addConverterFactory(MoshiConverterFactory.create(moshi))
//    .addCallAdapterFactory(CoroutineCallAdapterFactory())
//    .baseUrl(BASE_URL)
//    .build()


interface FlightApiService {
    @GET("screenshot")
    fun getScreenshotAsync(): Call<ResponseBody>

    @POST("api/command")
    fun postCommand(@Body command: Command): Call<Void>
}

//object FlightApi {
//    val retrofitService: FlightApiService by lazy {
//        retrofit.create(FlightApiService::class.java)
//    }
//}