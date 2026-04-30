package ke.go.moh.supervision.mobile.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supervision_records")
data class SupervisionRecordEntity(
    @PrimaryKey val id: String,
    val county: String,
    val subCounty: String,
    val chu: String,
    val facility: String,
    val levelOfSupervision: String,
    val whoAreRespondents: String,
    val respondentName: String,
    val comments: String,
    val actionPlan: String,
    val actionPlanDueDate: String,
    val recordStatus: String,
    val syncStatus: String,
    val updatedAt: Long,
)
