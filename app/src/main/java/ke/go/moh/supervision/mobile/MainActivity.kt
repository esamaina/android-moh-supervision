package ke.go.moh.supervision.mobile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ke.go.moh.supervision.mobile.data.SyncRepository
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
        val pushBtn = findViewById<Button>(R.id.pushSyncBtn)
        val pullBtn = findViewById<Button>(R.id.pullSyncBtn)
        val stateBtn = findViewById<Button>(R.id.stateBtn)
        val outputView = findViewById<TextView>(R.id.outputView)

        fun setLoading(loading: Boolean) {
            pushBtn.isEnabled = !loading
            pullBtn.isEnabled = !loading
            stateBtn.isEnabled = !loading
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
            val repository = SyncRepository(baseUrlInput.text.toString().trim())
            setLoading(true)
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        repository.push(username, password, deviceId)
                    }
                    outputView.text = "Push success:\n$result"
                } catch (e: Exception) {
                    outputView.text = "Push failed:\n${e.message}"
                } finally {
                    setLoading(false)
                }
            }
        }

        pullBtn.setOnClickListener {
            val (username, password, deviceId) = credentials()
            val repository = SyncRepository(baseUrlInput.text.toString().trim())
            setLoading(true)
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        repository.pull(username, password, deviceId)
                    }
                    outputView.text =
                        "Pull success:\nmessage=${result.message}\nrecords=${result.records.size}\npage=${result.page}"
                } catch (e: Exception) {
                    outputView.text = "Pull failed:\n${e.message}"
                } finally {
                    setLoading(false)
                }
            }
        }

        stateBtn.setOnClickListener {
            val (username, password, deviceId) = credentials()
            val repository = SyncRepository(baseUrlInput.text.toString().trim())
            setLoading(true)
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        repository.state(username, password, deviceId)
                    }
                    outputView.text =
                        "State:\nexists=${result.exists}\nlastSyncedAt=${result.lastSyncedAt}\nserverTime=${result.serverTime}"
                } catch (e: Exception) {
                    outputView.text = "State check failed:\n${e.message}"
                } finally {
                    setLoading(false)
                }
            }
        }
    }
}
