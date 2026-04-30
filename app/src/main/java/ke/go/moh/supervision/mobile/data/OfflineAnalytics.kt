package ke.go.moh.supervision.mobile.data

data class OfflineAnalytics(
    val total: Int,
    val completed: Int,
    val incomplete: Int,
    val synced: Int,
    val pendingSync: Int,
    val failedSync: Int,
    val byLevel: Map<String, Int>,
    val byRespondent: Map<String, Int>,
    val withActionPlan: Int,
)

fun buildOfflineAnalytics(records: List<SupervisionDraft>): OfflineAnalytics {
    val total = records.size
    val completed = records.count { it.recordStatus == "completed" }
    val synced = records.count { it.syncStatus == "synced" }
    val pendingSync = records.count { it.syncStatus != "synced" }
    val failedSync = records.count { it.syncStatus == "failed" }
    val byLevel = records.groupingBy { it.levelOfSupervision.ifBlank { "unknown" } }.eachCount()
    val byRespondent = records.groupingBy { it.whoAreRespondents.ifBlank { "unknown" } }.eachCount()
    val withActionPlan = records.count { it.actionPlan.isNotBlank() }

    return OfflineAnalytics(
        total = total,
        completed = completed,
        incomplete = total - completed,
        synced = synced,
        pendingSync = pendingSync,
        failedSync = failedSync,
        byLevel = byLevel,
        byRespondent = byRespondent,
        withActionPlan = withActionPlan
    )
}
