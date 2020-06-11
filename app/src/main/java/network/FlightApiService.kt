package network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private var moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

private const val BASE_URL =
    "https://10.0.2.2:64673"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()


interface FlightApiService {
    @GET("screenshot")
    fun getScreenshotAsync(): Deferred<String>
}

object FlightApi {
    val retrofitService: FlightApiService by lazy {
        retrofit.create(FlightApiService::class.java)
    }
}