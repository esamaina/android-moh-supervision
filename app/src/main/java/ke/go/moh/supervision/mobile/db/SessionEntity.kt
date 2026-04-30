package ke.go.moh.supervision.mobile.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val baseUrl: String,
    val username: String,
    val password: String,
    val role: String,
    val updatedAt: Long = System.currentTimeMillis(),
)
