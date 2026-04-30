package ke.go.moh.supervision.mobile.data

data class SyncRecord(
    val id: String,
    val status: String = "incomplete",
    val form_data: Map<String, Any?>? = null,
    val leadership: Map<String, Any?>? = null,
    val workforce: Map<String, Any?>? = null,
    val infrastructure: Map<String, Any?>? = null,
    val monitoringandevaluation: Map<String, Any?>? = null,
    val commodities: Map<String, Any?>? = null,
    val transport: Map<String, Any?>? = null,
    val referral: Map<String, Any?>? = null,
    val finance: Map<String, Any?>? = null,
    val partnership: Map<String, Any?>? = null,
    val pandemic: Map<String, Any?>? = null,
    val supervision: Map<String, Any?>? = null,
    val servicedelivery: Map<String, Any?>? = null,
    val actionplan: Map<String, Any?>? = null,
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

data class PillarLevelCoverage(
    val county: Boolean = false,
    val subCounty: Boolean = false,
    val chu: Boolean = false,
    val facility: Boolean = false,
)

data class QuestionnairePillar(
    val pillar: String,
    val levels: PillarLevelCoverage,
)

data class QuestionnaireSchemaResponse(
    val success: Boolean,
    val schemaVersion: String,
    val schemaUpdatedAt: String? = null,
    val levels: List<String> = emptyList(),
    val pillars: List<QuestionnairePillar> = emptyList(),
    val allPillarsHaveAllLevels: Boolean = false,
)

data class MobileUser(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val county: String? = null,
    val sub_county: String? = null,
    val chu: String? = null,
    val user_status: String = "active",
)

data class MobileUsersResponse(
    val users: List<MobileUser> = emptyList(),
)
