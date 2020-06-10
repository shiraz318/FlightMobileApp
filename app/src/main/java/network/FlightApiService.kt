package network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

private const val BASE_URL =
    "https://localhost:64673"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()


interface FlightApiService {
    @GET("screenshot")
    fun getScreenshot():
            Call<String>
}

object FlightApi {
    val retrofitService: FlightApiService by lazy {
        retrofit.create(FlightApiService::class.java)
    }
}