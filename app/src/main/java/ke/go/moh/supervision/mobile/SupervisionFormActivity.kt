package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ke.go.moh.supervision.mobile.data.SupervisionDraft
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore
import ke.go.moh.supervision.mobile.data.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupervisionFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervision_form)

        val session = SessionManager(this)
        val store = SupervisionDraftStore(this)
        val draftId = intent.getStringExtra("draftId")
        var draft = draftId?.let { id -> store.loadAll().firstOrNull { it.id == id } } ?: store.load()

        val countyInput = findViewById<EditText>(R.id.countyInput)
        val subCountyInput = findViewById<EditText>(R.id.subCountyInput)
        val chuInput = findViewById<EditText>(R.id.chuInput)
        val facilityInput = findViewById<EditText>(R.id.facilityInput)
        val respondentTypeInput = findViewById<EditText>(R.id.respondentTypeInput)
        val respondentNameInput = findViewById<EditText>(R.id.respondentNameInput)
        val commentsInput = findViewById<EditText>(R.id.commentsInput)
        val levelSpinner = findViewById<Spinner>(R.id.levelSpinner)
        val statusView = findViewById<TextView>(R.id.statusView)
        val saveBtn = findViewById<Button>(R.id.saveDraftBtn)
        val submitBtn = findViewById<Button>(R.id.submitBtn)
        val clearBtn = findViewById<Button>(R.id.clearDraftBtn)
        val actionPlanBtn = findViewById<Button>(R.id.actionPlanBtn)
        val recordStatusSpinner = findViewById<Spinner>(R.id.recordStatusSpinner)

        val levels = listOf("facility", "chu", "sub-county", "county")
        val statuses = listOf("incomplete", "completed")
        levelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, levels)
        recordStatusSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)

        fun bind(d: SupervisionDraft) {
            countyInput.setText(d.county)
            subCountyInput.setText(d.subCounty)
            chuInput.setText(d.chu)
            facilityInput.setText(d.facility)
            respondentTypeInput.setText(d.whoAreRespondents)
            respondentNameInput.setText(d.respondentName)
            commentsInput.setText(d.comments)
            val idx = levels.indexOf(d.levelOfSupervision).coerceAtLeast(0)
            levelSpinner.setSelection(idx)
            val statusIdx = statuses.indexOf(d.recordStatus).coerceAtLeast(0)
            recordStatusSpinner.setSelection(statusIdx)
            statusView.text = "Draft ID: ${d.id} | Record: ${d.recordStatus} | Sync: ${d.syncStatus}"
        }

        fun collect(): SupervisionDraft {
            return draft.copy(
                county = countyInput.text.toString().trim(),
                subCounty = subCountyInput.text.toString().trim(),
                chu = chuInput.text.toString().trim(),
                facility = facilityInput.text.toString().trim(),
                levelOfSupervision = levelSpinner.selectedItem.toString(),
                whoAreRespondents = respondentTypeInput.text.toString().trim(),
                respondentName = respondentNameInput.text.toString().trim(),
                comments = commentsInput.text.toString().trim(),
                recordStatus = recordStatusSpinner.selectedItem.toString(),
                syncStatus = "draft",
                updatedAt = System.currentTimeMillis()
            )
        }

        bind(draft)

        saveBtn.setOnClickListener {
            draft = collect()
            store.save(draft)
            Toast.makeText(this, "Draft saved locally", Toast.LENGTH_SHORT).show()
            bind(draft)
        }

        submitBtn.setOnClickListener {
            val username = session.getUsername()
            val password = session.getPassword()
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Login first to submit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            draft = collect()
            store.save(draft)
            saveBtn.isEnabled = false
            submitBtn.isEnabled = false
            clearBtn.isEnabled = false
            statusView.text = "Submitting..."

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        SyncRepository(session.getBaseUrl()).pushDraft(
                            username = username,
                            password = password,
                            deviceId = "android-dev-001",
                            draft = draft
                        )
                    }
                    draft = draft.copy(syncStatus = "synced", updatedAt = System.currentTimeMillis())
                    store.save(draft)
                    statusView.text = "Submitted: $response"
                    Toast.makeText(
                        this@SupervisionFormActivity,
                        "Draft pushed to backend sync",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    draft = draft.copy(syncStatus = "failed", updatedAt = System.currentTimeMillis())
                    store.save(draft)
                    statusView.text = "Submit failed: ${e.message}"
                } finally {
                    saveBtn.isEnabled = true
                    submitBtn.isEnabled = true
                    clearBtn.isEnabled = true
                }
            }
        }

        clearBtn.setOnClickListener {
            store.remove(draft.id)
            draft = SupervisionDraft()
            bind(draft)
            Toast.makeText(this, "Draft removed", Toast.LENGTH_SHORT).show()
        }

        actionPlanBtn.setOnClickListener {
            draft = collect()
            store.save(draft)
            val intent = Intent(this, ActionPlanActivity::class.java)
            intent.putExtra("draftId", draft.id)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val store = SupervisionDraftStore(this)
        val latest = intent.getStringExtra("draftId")?.let { id ->
            store.loadAll().firstOrNull { it.id == id }
        } ?: store.load()
        findViewById<TextView>(R.id.statusView).text =
            "Draft ID: ${latest.id} | Record: ${latest.recordStatus} | Sync: ${latest.syncStatus}"
    }
}
