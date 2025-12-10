package es.mirumi.es.services
import retrofit2.Call
import retrofit2.http.GET

interface TestApiService {
    @GET("api/hello")
    fun getHello(): Call<String>
}
