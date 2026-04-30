package ke.go.moh.supervision.mobile.data

import android.content.Context
import ke.go.moh.supervision.mobile.db.AppDatabase

data class QueueMetrics(
    val pending: Int,
    val retried: Int,
    val failed: Int,
    val lastErrors: List<String> = emptyList()
)

class SyncQueueRepository(context: Context) {
    private val dao = AppDatabase.get(context).syncQueueDao()

    suspend fun metrics(): QueueMetrics = QueueMetrics(
        pending = dao.countPending(),
        retried = dao.countRetried(),
        failed = dao.countFailed(),
        lastErrors = dao.pending()
            .mapNotNull { it.lastError.takeIf { msg -> msg.isNotBlank() } }
            .distinct()
            .take(3)
    )

    suspend fun clearQueue() {
        dao.pending().forEach { dao.delete(it.queueId) }
    }

    suspend fun resetFailedQueue() {
        dao.pending().forEach { item ->
            if (item.lastError.isNotBlank()) {
                dao.delete(item.queueId)
                dao.enqueue(
                    item.copy(
                        queueId = 0,
                        attempts = 0,
                        lastError = "",
                        enqueuedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
