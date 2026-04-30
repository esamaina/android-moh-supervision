package ke.go.moh.supervision.mobile.data

import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class UserRepository(baseUrl: String) {
    private val service: UserApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        service = Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UserApiService::class.java)
    }

    private fun basicAuth(username: String, password: String): String {
        val token = Base64.encodeToString(
            "$username:$password".toByteArray(),
            Base64.NO_WRAP
        )
        return "Basic $token"
    }

    suspend fun getUsers(username: String, password: String): List<MobileUser> =
        service.getUsers(basicAuth(username, password)).users

    suspend fun toggleUserStatus(
        username: String,
        password: String,
        user: MobileUser
    ): Map<String, Any?> {
        val nextStatus = if (user.user_status == "active") "inactive" else "active"
        return service.updateUserStatus(
            basicAuth(username, password),
            user.id,
            mapOf("user_status" to nextStatus)
        )
    }

    suspend fun changePassword(
        username: String,
        password: String,
        userId: String,
        newPassword: String
    ): Map<String, Any?> {
        return service.changeUserPassword(
            basicAuth(username, password),
            userId,
            mapOf("newPassword" to newPassword)
        )
    }

    suspend fun createUser(
        username: String,
        password: String,
        email: String,
        newUsername: String,
        newPassword: String,
        role: String
    ): Map<String, Any?> {
        return service.createUser(
            basicAuth(username, password),
            mapOf(
                "email" to email,
                "username" to newUsername,
                "password" to newPassword,
                "newRole" to role
            )
        )
    }
}
