package ke.go.moh.supervision.mobile.data

import java.util.UUID

data class SupervisionDraft(
    val id: String = UUID.randomUUID().toString(),
    val county: String = "",
    val subCounty: String = "",
    val chu: String = "",
    val facility: String = "",
    val levelOfSupervision: String = "facility",
    val whoAreRespondents: String = "",
    val respondentName: String = "",
    val comments: String = "",
    val actionPlan: String = "",
    val actionPlanDueDate: String = "",
    val allPillarsPayloadJson: String = "{}",
    val recordStatus: String = "incomplete",
    val syncStatus: String = "draft",
    val conflictPolicy: String = "none",
    val updatedAt: Long = System.currentTimeMillis(),
)
