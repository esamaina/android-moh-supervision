package ke.go.moh.supervision.mobile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore
import ke.go.moh.supervision.mobile.data.SyncQueueRepository
import ke.go.moh.supervision.mobile.data.SyncRepository
import ke.go.moh.supervision.mobile.sync.DeviceIdProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val baseUrlInput = findViewById<EditText>(R.id.baseUrlInput)
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val deviceIdInput = findViewById<EditText>(R.id.deviceIdInput)
        val backBtn = findViewById<Button>(R.id.backMenuBtn)
        val pushBtn = findViewById<Button>(R.id.pushSyncBtn)
        val pullBtn = findViewById<Button>(R.id.pullSyncBtn)
        val stateBtn = findViewById<Button>(R.id.stateBtn)
        val retryFailedBtn = findViewById<Button>(R.id.retryFailedBtn)
        val clearQueueBtn = findViewById<Button>(R.id.clearQueueBtn)
        val outputView = findViewById<TextView>(R.id.outputView)
        val session = SessionManager(this)
        val deviceIdProvider = DeviceIdProvider(this)
        val store = SupervisionDraftStore(this)
        val queueRepo = SyncQueueRepository(this)

        baseUrlInput.setText(session.getBaseUrl())
        usernameInput.setText(session.getUsername())
        passwordInput.setText(session.getPassword())
        deviceIdInput.setText(deviceIdProvider.getOrCreate())

        backBtn.setOnClickListener { finish() }
        suspend fun syncHealthLine(): String {
            val all = store.loadAll()
            val pending = all.count { it.syncStatus != "synced" }
            val failed = all.count { it.syncStatus == "failed" }
            val queue = queueRepo.metrics()
            val errors = if (queue.lastErrors.isEmpty()) "none" else queue.lastErrors.joinToString(" | ")
            return "Sync health -> pending: $pending, failed: $failed, total: ${all.size}\nQueue -> pending: ${queue.pending}, retried: ${queue.retried}, failedQueue: ${queue.failed}\nLast errors: $errors"
        }

        fun setLoading(loading: Boolean) {
            pushBtn.isEnabled = !loading
            pullBtn.isEnabled = !loading
            stateBtn.isEnabled = !loading
            retryFailedBtn.isEnabled = !loading
            clearQueueBtn.isEnabled = !loading
        }

        fun credentials(): Triple<String, String, String> {
            return Triple(
                usernameInput.text.toString().trim(),
                passwordInput.text.toString(),
                deviceIdInput.text.toString().trim()
            )
        }

        pushBtn.setOnClickListener {
            val (username, password, deviceId) = credentials()
            if (username.isBlank() || password.isBlank() || deviceId.isBlank()) {
                Toast.makeText(this, "Enter username, password and device ID", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            session.saveBaseUrl(baseUrlInput.text.toString().trim())
            session.saveUsername(username)
            session.savePassword(password)
            deviceIdProvider.save(deviceId)
            val repository = SyncRepository(baseUrlInput.text.toString().trim())
            setLoading(true)
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        repository.push(username, password, deviceId)
                    }
                    val health = withContext(Dispatchers.IO) { syncHealthLine() }
                    outputView.text = "Push success:\n$result\n$health"
                } catch (e: Exception) {
                    val health = withContext(Dispatchers.IO) { syncHealthLine() }
                    outputView.text = "Push failed:\n${e.message}\n$health"
                } finally {
                    setLoading(false)
                }
            }
        }

        pullBtn.setOnClickListener {
            val (username, password, deviceId) = credentials()
            if (username.isBlank() || password.isBlank() || deviceId.isBlank()) {
                Toast.makeText(this, "Enter username, password and device ID", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val repository = SyncRepository(baseUrlInput.text.toString().trim())
            setLoading(true)
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        repository.pull(username, password, deviceId)
                    }
                    val health = withContext(Dispatchers.IO) { syncHealthLine() }
                    outputView.text =
                        "Pull success:\nmessage=${result.message}\nrecords=${result.records.size}\npage=${result.page}\n$health"
                } catch (e: Exception) {
                    val health = withContext(Dispatchers.IO) { syncHealthLine() }
                    outputView.text = "Pull failed:\n${e.message}\n$health"
                } finally {
                    setLoading(false)
                }
            }
        }

        stateBtn.setOnClickListener {
            val (username, password, deviceId) = credentials()
            if (username.isBlank() || password.isBlank() || deviceId.isBlank()) {
                Toast.makeText(this, "Enter username, password and device ID", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val repository = SyncRepository(baseUrlInput.text.toString().trim())
            setLoading(true)
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        repository.state(username, password, deviceId)
                    }
                    val health = withContext(Dispatchers.IO) { syncHealthLine() }
                    outputView.text =
                        "State:\nexists=${result.exists}\nlastSyncedAt=${result.lastSyncedAt}\nserverTime=${result.serverTime}\n$health"
                } catch (e: Exception) {
                    val health = withContext(Dispatchers.IO) { syncHealthLine() }
                    outputView.text = "State check failed:\n${e.message}\n$health"
                } finally {
                    setLoading(false)
                }
            }
        }

        retryFailedBtn.setOnClickListener {
            setLoading(true)
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) { queueRepo.resetFailedQueue() }
                    val health = withContext(Dispatchers.IO) { syncHealthLine() }
                    outputView.text = "Retry queue reset complete.\n$health"
                } catch (e: Exception) {
                    outputView.text = "Retry queue reset failed: ${e.message}"
                } finally {
                    setLoading(false)
                }
            }
        }

        clearQueueBtn.setOnClickListener {
            setLoading(true)
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) { queueRepo.clearQueue() }
                    val health = withContext(Dispatchers.IO) { syncHealthLine() }
                    outputView.text = "Queue cleared.\n$health"
                } catch (e: Exception) {
                    outputView.text = "Clear queue failed: ${e.message}"
                } finally {
                    setLoading(false)
                }
            }
        }
    }
}
