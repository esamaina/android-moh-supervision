package ke.go.moh.supervision.mobile.data

data class SyncRecord(
    val id: String,
    val status: String = "incomplete",
    val form_data: Map<String, Any?>? = null,
)

data class PushSyncRequest(
    val deviceId: String,
    val records: List<SyncRecord>,
)

data class PullSyncResponse(
    val success: Boolean,
    val message: String,
    val records: List<Map<String, Any?>> = emptyList(),
    val page: Map<String, Any?>? = null,
    val serverTime: String? = null,
)

data class StateResponse(
    val success: Boolean,
    val exists: Boolean,
    val deviceId: String,
    val lastSyncedAt: String?,
    val serverTime: String? = null,
)
