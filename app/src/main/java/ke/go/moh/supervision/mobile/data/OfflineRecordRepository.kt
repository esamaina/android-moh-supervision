package ke.go.moh.supervision.mobile.data

import android.content.Context
import ke.go.moh.supervision.mobile.db.AppDatabase
import ke.go.moh.supervision.mobile.db.SupervisionRecordEntity

class OfflineRecordRepository(context: Context) {
    private val dao = AppDatabase.get(context).supervisionDao()
    private val queueDao = AppDatabase.get(context).syncQueueDao()

    suspend fun save(draft: SupervisionDraft) {
        dao.upsert(draft.toEntity())
        queueDao.enqueue(
            ke.go.moh.supervision.mobile.db.SyncQueueEntity(
                recordId = draft.id,
                operation = "upsert"
            )
        )
    }

    suspend fun getAll(): List<SupervisionDraft> = dao.getAll().map { it.toDraft() }
    suspend fun get(id: String): SupervisionDraft? = dao.getById(id)?.toDraft()
    suspend fun remove(id: String) = dao.deleteById(id)
}

fun SupervisionDraft.toEntity(): SupervisionRecordEntity = SupervisionRecordEntity(
    id = id,
    county = county,
    subCounty = subCounty,
    chu = chu,
    facility = facility,
    levelOfSupervision = levelOfSupervision,
    whoAreRespondents = whoAreRespondents,
    respondentName = respondentName,
    comments = comments,
    actionPlan = actionPlan,
    actionPlanDueDate = actionPlanDueDate,
    recordStatus = recordStatus,
    syncStatus = syncStatus,
    updatedAt = updatedAt
)

fun SupervisionRecordEntity.toDraft(): SupervisionDraft = SupervisionDraft(
    id = id,
    county = county,
    subCounty = subCounty,
    chu = chu,
    facility = facility,
    levelOfSupervision = levelOfSupervision,
    whoAreRespondents = whoAreRespondents,
    respondentName = respondentName,
    comments = comments,
    actionPlan = actionPlan,
    actionPlanDueDate = actionPlanDueDate,
    recordStatus = recordStatus,
    syncStatus = syncStatus,
    updatedAt = updatedAt
)
