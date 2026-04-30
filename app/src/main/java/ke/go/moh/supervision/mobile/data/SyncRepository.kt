package ke.go.moh.supervision.mobile.data

import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID

class SyncRepository(baseUrl: String) {
    private val service: SyncApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        service = Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SyncApiService::class.java)
    }

    private fun basicAuth(username: String, password: String): String {
        val token = Base64.encodeToString(
            "$username:$password".toByteArray(),
            Base64.NO_WRAP
        )
        return "Basic $token"
    }

    suspend fun push(username: String, password: String, deviceId: String): Map<String, Any?> {
        val auth = basicAuth(username, password)
        val sampleRecord = SyncRecord(
            id = UUID.randomUUID().toString(),
            status = "incomplete",
            form_data = mapOf(
                "SupervisionTeam" to mapOf(
                    "county" to "Demo County",
                    "subCounty" to "Demo SubCounty",
                    "levelOfSupervision" to "facility"
                )
            )
        )
        return service.pushSync(
            auth = auth,
            deviceId = deviceId,
            request = PushSyncRequest(deviceId = deviceId, records = listOf(sampleRecord))
        )
    }

    suspend fun pull(username: String, password: String, deviceId: String): PullSyncResponse {
        return service.pullSync(
            auth = basicAuth(username, password),
            deviceId = deviceId
        )
    }

    suspend fun state(username: String, password: String, deviceId: String): StateResponse {
        return service.getState(
            auth = basicAuth(username, password),
            deviceId = deviceId
        )
    }
}
