package ke.go.moh.supervision.mobile.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApiService {
    @POST("/api/mobile/sync")
    suspend fun pushSync(
        @Header("Authorization") auth: String,
        @Header("x-device-id") deviceId: String,
        @Body request: PushSyncRequest,
    ): Map<String, Any?>

    @GET("/api/mobile/sync")
    suspend fun pullSync(
        @Header("Authorization") auth: String,
        @Header("x-device-id") deviceId: String,
        @Query("since") since: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("status") status: String = "all",
        @Query("cursorUpdatedAt") cursorUpdatedAt: String? = null,
        @Query("cursorId") cursorId: String? = null,
    ): PullSyncResponse

    @GET("/api/mobile/sync/state")
    suspend fun getState(
        @Header("Authorization") auth: String,
        @Header("x-device-id") deviceId: String,
    ): StateResponse
}
