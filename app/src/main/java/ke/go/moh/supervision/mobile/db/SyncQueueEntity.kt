package ke.go.moh.supervision.mobile.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val queueId: Long = 0,
    val recordId: String,
    val operation: String,
    val attempts: Int = 0,
    val lastError: String = "",
    val enqueuedAt: Long = System.currentTimeMillis(),
)
