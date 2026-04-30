package ke.go.moh.supervision.mobile

import android.content.Context
import ke.go.moh.supervision.mobile.data.SecureSessionRepository
import kotlinx.coroutines.runBlocking

class SessionManager(context: Context) {
    private val secureRepo = SecureSessionRepository(context)
    private val prefs = context.getSharedPreferences("sst_ui_mode", Context.MODE_PRIVATE)

    fun saveBaseUrl(baseUrl: String) = save(
        baseUrl = baseUrl,
        username = getUsername(),
        password = getPassword(),
        role = getRole()
    )
    fun saveUsername(username: String) = save(
        baseUrl = getBaseUrl(),
        username = username,
        password = getPassword(),
        role = getRole()
    )
    fun savePassword(password: String) = save(
        baseUrl = getBaseUrl(),
        username = getUsername(),
        password = password,
        role = getRole()
    )
    fun saveRole(role: String) = save(
        baseUrl = getBaseUrl(),
        username = getUsername(),
        password = getPassword(),
        role = role
    )

    private fun save(baseUrl: String, username: String, password: String, role: String) {
        runBlocking { secureRepo.save(baseUrl, username, password, role) }
    }

    fun getBaseUrl(): String = runBlocking {
        secureRepo.get()?.baseUrl ?: "https://chw-supervision.echis.go.ke/"
    }
    fun getUsername(): String = runBlocking { secureRepo.get()?.username ?: "" }
    fun getPassword(): String = runBlocking { secureRepo.get()?.password ?: "" }
    fun getRole(): String = runBlocking { secureRepo.get()?.role ?: "user" }

    fun hasCredentials(): Boolean = getUsername().isNotBlank() && getPassword().isNotBlank()
    fun clearSession() = runBlocking { secureRepo.clear() }

    fun setMode(mode: String) {
        val safe = if (mode == "offline") "offline" else "online"
        prefs.edit().putString("mode", safe).apply()
    }

    fun getMode(): String = prefs.getString("mode", "online") ?: "online"
}
