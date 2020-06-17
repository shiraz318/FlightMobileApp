package network

import com.example.flightmobileapp.Command
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface FlightApiService {
    @GET("screenshot")
    suspend fun getScreenshotAsync(): Response<ResponseBody>

    @POST("api/command")
    suspend fun postCommand(@Body command: Command): Response<Void>
}
