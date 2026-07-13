package hu.tutiar.tutirrendelsek.network

import hu.tutiar.tutirrendelsek.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("tutiar-api-sync.php")
    suspend fun sync(
        @Header("Authorization") authorization: String,
        @Query("action") action: String = "sync",
        @Query("since") since: String? = null
    ): ApiResponse
}
