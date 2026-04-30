package ke.go.moh.supervision.mobile.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ke.go.moh.supervision.mobile.SessionManager
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
        val repo = SyncRepository(session.getBaseUrl())
        val drafts = store.loadAll().filter { it.syncStatus != "synced" }
        drafts.forEach { draft ->
            try {
                repo.pushDraft(username, password, "android-dev-001", draft)
                store.save(draft.copy(syncStatus = "synced", updatedAt = System.currentTimeMillis()))
            } catch (e: Exception) {
                store.save(draft.copy(syncStatus = "failed", updatedAt = System.currentTimeMillis()))
            }
        }
        return Result.success()
    }
}
