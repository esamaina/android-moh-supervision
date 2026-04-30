package ke.go.moh.supervision.mobile.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ke.go.moh.supervision.mobile.SessionManager
import ke.go.moh.supervision.mobile.db.AppDatabase
import ke.go.moh.supervision.mobile.data.QuestionnaireSchemaManager
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore
import ke.go.moh.supervision.mobile.data.SyncRepository

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val session = SessionManager(applicationContext)
        val username = session.getUsername()
        val password = session.getPassword()
        if (username.isBlank() || password.isBlank()) return Result.success()

        val store = SupervisionDraftStore(applicationContext)
        val schemaManager = QuestionnaireSchemaManager(applicationContext)
        val queueDao = AppDatabase.get(applicationContext).syncQueueDao()
        val repo = SyncRepository(session.getBaseUrl())
        val deviceId = DeviceIdProvider(applicationContext).getOrCreate()
        try {
            val schema = repo.questionnaireSchema(username, password)
            schemaManager.saveVersion(schema.schemaVersion, coverageOk = schema.allPillarsHaveAllLevels)
        } catch (_: Exception) {
            // Keep offline work going even when schema refresh fails.
        }
        val pendingQueue = queueDao.pending()
        val draftMap = store.loadAll().associateBy { it.id }
        pendingQueue.forEach { item ->
            val draft = draftMap[item.recordId] ?: run {
                queueDao.delete(item.queueId)
                return@forEach
            }
            if (shouldSkipPushByPolicy(draft)) {
                // Honor explicit server-wins policy by dropping local pending queue work.
                store.save(
                    draftAfterSyncSuccess(draft, System.currentTimeMillis()),
                    enqueueForSync = false
                )
                queueDao.delete(item.queueId)
                return@forEach
            }
            try {
                repo.pushDraft(username, password, deviceId, draft)
                store.save(
                    draftAfterSyncSuccess(draft, System.currentTimeMillis()),
                    enqueueForSync = false
                )
                queueDao.delete(item.queueId)
            } catch (e: Exception) {
                store.save(
                    draftAfterSyncFailure(draft, System.currentTimeMillis()),
                    enqueueForSync = false
                )
                queueDao.markFailure(item.queueId, e.message ?: "Unknown sync error")
            }
        }
        return Result.success()
    }
}
