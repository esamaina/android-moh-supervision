package ke.go.moh.supervision.mobile.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import ke.go.moh.supervision.mobile.db.AppDatabase
import ke.go.moh.supervision.mobile.db.SessionEntity

class SecureSessionRepository(context: Context) {
    private val sessionDao = AppDatabase.get(context).sessionDao()
    private val prefs by lazy {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "sst_secure_prefs",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun save(baseUrl: String, username: String, password: String, role: String) {
        sessionDao.upsert(
            SessionEntity(
                baseUrl = baseUrl,
                username = username,
                password = password,
                role = role
            )
        )
        prefs.edit().putBoolean("hasSession", true).apply()
    }

    suspend fun get(): SessionEntity? = sessionDao.get()
    suspend fun clear() {
        sessionDao.clear()
        prefs.edit().clear().apply()
    }

    fun hasSessionFlag(): Boolean = prefs.getBoolean("hasSession", false)
}
