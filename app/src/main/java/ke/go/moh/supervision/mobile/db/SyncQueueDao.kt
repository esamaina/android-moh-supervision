package ke.go.moh.supervision.mobile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue ORDER BY enqueuedAt ASC")
    suspend fun pending(): List<SyncQueueEntity>

    @Query("DELETE FROM sync_queue WHERE queueId = :queueId")
    suspend fun delete(queueId: Long)

    @Query("UPDATE sync_queue SET attempts = attempts + 1, lastError = :error WHERE queueId = :queueId")
    suspend fun markFailure(queueId: Long, error: String)
}
