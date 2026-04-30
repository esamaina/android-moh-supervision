package ke.go.moh.supervision.mobile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: SessionEntity)

    @Query("SELECT * FROM session WHERE id = 1 LIMIT 1")
    suspend fun get(): SessionEntity?

    @Query("DELETE FROM session WHERE id = 1")
    suspend fun clear()
}
