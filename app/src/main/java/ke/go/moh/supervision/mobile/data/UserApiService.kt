package ke.go.moh.supervision.mobile.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApiService {
    @GET("/api/mobile/users")
    suspend fun getUsers(
        @Header("Authorization") auth: String
    ): MobileUsersResponse

    @PATCH("/api/mobile/users/{id}/status")
    suspend fun updateUserStatus(
        @Header("Authorization") auth: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Map<String, Any?>

    @POST("/api/mobile/users/{id}/change-password")
    suspend fun changeUserPassword(
        @Header("Authorization") auth: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Map<String, Any?>
}
