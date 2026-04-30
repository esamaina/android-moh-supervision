package ke.go.moh.supervision.mobile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SupervisionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: SupervisionRecordEntity)

    @Query("SELECT * FROM supervision_records ORDER BY updatedAt DESC")
    suspend fun getAll(): List<SupervisionRecordEntity>

    @Query("SELECT * FROM supervision_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SupervisionRecordEntity?

    @Query("DELETE FROM supervision_records WHERE id = :id")
    suspend fun deleteById(id: String)
}
