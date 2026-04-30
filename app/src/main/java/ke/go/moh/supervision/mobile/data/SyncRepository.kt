package ke.go.moh.supervision.mobile.data

import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

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

    private fun jsonValueToKotlin(value: Any?): Any? {
        return when (value) {
            null, JSONObject.NULL -> null
            is JSONObject -> value.keys().asSequence().associateWith { key ->
                jsonValueToKotlin(value.opt(key))
            }
            is JSONArray -> (0 until value.length()).map { idx -> jsonValueToKotlin(value.opt(idx)) }
            else -> value
        }
    }

    private fun jsonToMap(value: String): Map<String, Any?> {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return emptyMap()
        return try {
            val obj = JSONObject(trimmed)
            obj.keys().asSequence().associateWith { key -> jsonValueToKotlin(obj.opt(key)) }
        } catch (_: Exception) {
            emptyMap()
        }
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

    suspend fun pushDraft(
        username: String,
        password: String,
        deviceId: String,
        draft: SupervisionDraft
    ): Map<String, Any?> {
        val auth = basicAuth(username, password)
        val payload = jsonToMap(draft.allPillarsPayloadJson)
        val payloadFormData = payload["form_data"] as? Map<String, Any?> ?: emptyMap()
        val supervisionTeam = ((payloadFormData["SupervisionTeam"] as? Map<String, Any?>) ?: emptyMap()) + mapOf(
            "county" to draft.county,
            "subCounty" to draft.subCounty,
            "chu" to draft.chu,
            "facility" to draft.facility,
            "levelOfSupervision" to draft.levelOfSupervision,
            "whoAreRespondents" to draft.whoAreRespondents,
            "respondentName" to draft.respondentName
        )
        val mergedFormData = payloadFormData + mapOf(
            "SupervisionTeam" to supervisionTeam,
            "supervision" to (((payloadFormData["supervision"] as? Map<String, Any?>) ?: emptyMap()) + mapOf(
                "comments" to draft.comments
            )),
            "actionplan" to (((payloadFormData["actionplan"] as? Map<String, Any?>) ?: emptyMap()) + mapOf(
                "plan" to draft.actionPlan,
                "dueDate" to draft.actionPlanDueDate
            ))
        )
        val record = SyncRecord(
            id = draft.id,
            status = draft.recordStatus,
            form_data = mergedFormData,
            leadership = payload["leadership"] as? Map<String, Any?>,
            workforce = payload["workforce"] as? Map<String, Any?>,
            infrastructure = payload["infrastructure"] as? Map<String, Any?>,
            monitoringandevaluation = payload["monitoringandevaluation"] as? Map<String, Any?>,
            commodities = payload["commodities"] as? Map<String, Any?>,
            transport = payload["transport"] as? Map<String, Any?>,
            referral = payload["referral"] as? Map<String, Any?>,
            finance = payload["finance"] as? Map<String, Any?>,
            partnership = payload["partnership"] as? Map<String, Any?>,
            pandemic = payload["pandemic"] as? Map<String, Any?>,
            supervision = payload["supervision"] as? Map<String, Any?>,
            servicedelivery = payload["servicedelivery"] as? Map<String, Any?>,
            actionplan = payload["actionplan"] as? Map<String, Any?>,
        )
        return service.pushSync(
            auth = auth,
            deviceId = deviceId,
            request = PushSyncRequest(deviceId = deviceId, records = listOf(record))
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

    suspend fun questionnaireSchema(
        username: String,
        password: String
    ): QuestionnaireSchemaResponse {
        return service.getQuestionnaireSchema(
            auth = basicAuth(username, password)
        )
    }
}
