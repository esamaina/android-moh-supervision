package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ke.go.moh.supervision.mobile.data.QuestionnaireSchemaManager
import ke.go.moh.supervision.mobile.data.SupervisionDraft
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore
import ke.go.moh.supervision.mobile.data.SyncRepository
import ke.go.moh.supervision.mobile.data.normalizeAllPillarsPayload
import ke.go.moh.supervision.mobile.sync.DeviceIdProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SupervisionFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervision_form)

        val session = SessionManager(this)
        val deviceIdProvider = DeviceIdProvider(this)
        val store = SupervisionDraftStore(this)
        val schemaManager = QuestionnaireSchemaManager(this)
        val draftId = intent.getStringExtra("draftId")
        var draft = draftId?.let { id -> store.loadAll().firstOrNull { it.id == id } } ?: store.load()

        val countyInput = findViewById<EditText>(R.id.countyInput)
        val subCountyInput = findViewById<EditText>(R.id.subCountyInput)
        val chuInput = findViewById<EditText>(R.id.chuInput)
        val facilityInput = findViewById<EditText>(R.id.facilityInput)
        val respondentTypeInput = findViewById<EditText>(R.id.respondentTypeInput)
        val respondentNameInput = findViewById<EditText>(R.id.respondentNameInput)
        val commentsInput = findViewById<EditText>(R.id.commentsInput)
        val pillarSpinner = findViewById<Spinner>(R.id.pillarSpinner)
        val questionKeyInput = findViewById<EditText>(R.id.questionKeyInput)
        val questionValueInput = findViewById<EditText>(R.id.questionValueInput)
        val addQuestionBtn = findViewById<Button>(R.id.addQuestionBtn)
        val pillarPreviewView = findViewById<TextView>(R.id.pillarPreviewView)
        val allPillarsPayloadInput = findViewById<EditText>(R.id.allPillarsPayloadInput)
        val levelSpinner = findViewById<Spinner>(R.id.levelSpinner)
        val statusView = findViewById<TextView>(R.id.statusView)
        val scoreView = findViewById<TextView>(R.id.scoreView)
        val schemaStatusView = findViewById<TextView>(R.id.schemaStatusView)
        val sectionView = findViewById<TextView>(R.id.sectionView)
        val prevBtn = findViewById<Button>(R.id.prevSectionBtn)
        val nextBtn = findViewById<Button>(R.id.nextSectionBtn)
        val saveBtn = findViewById<Button>(R.id.saveDraftBtn)
        val submitBtn = findViewById<Button>(R.id.submitBtn)
        val clearBtn = findViewById<Button>(R.id.clearDraftBtn)
        val actionPlanBtn = findViewById<Button>(R.id.actionPlanBtn)
        val recordStatusSpinner = findViewById<Spinner>(R.id.recordStatusSpinner)

        val levels = listOf("facility", "chu", "sub-county", "county")
        val pillars = listOf(
            "leadership",
            "workforce",
            "infrastructure",
            "monitoringandevaluation",
            "commodities",
            "transport",
            "referral",
            "finance",
            "partnership",
            "pandemic",
            "supervision",
            "servicedelivery",
            "actionplan"
        )
        val statuses = listOf("incomplete", "completed")
        val sections = listOf("Location", "Respondent", "Assessment")
        var sectionIndex = 0
        levelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, levels)
        pillarSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, pillars)
        recordStatusSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)

        fun draftScore(d: SupervisionDraft): Double {
            var points = 0.0
            if (d.county.isNotBlank()) points += 1
            if (d.subCounty.isNotBlank()) points += 1
            if (d.levelOfSupervision != "county" && d.chu.isNotBlank()) points += 1
            if (d.levelOfSupervision == "facility" && d.facility.isNotBlank()) points += 1
            if (d.whoAreRespondents.isNotBlank()) points += 1
            if (d.respondentName.isNotBlank()) points += 1
            if (d.comments.isNotBlank()) points += 1
            if (d.actionPlan.isNotBlank()) points += 1
            return (points / 8.0) * 100.0
        }

        fun applyLevelVisibility(level: String) {
            countyInput.hint = "County"
            subCountyInput.visibility = View.VISIBLE
            chuInput.visibility = View.VISIBLE
            facilityInput.visibility = View.VISIBLE
            when (level) {
                "county" -> {
                    subCountyInput.visibility = View.GONE
                    chuInput.visibility = View.GONE
                    facilityInput.visibility = View.GONE
                }
                "sub-county" -> {
                    facilityInput.visibility = View.GONE
                }
                "chu" -> {
                    facilityInput.visibility = View.GONE
                }
            }
        }

        fun applySectionVisibility() {
            val isLocation = sectionIndex == 0
            val isRespondent = sectionIndex == 1
            val isAssessment = sectionIndex == 2
            countyInput.visibility = if (isLocation) View.VISIBLE else View.GONE
            subCountyInput.visibility = if (isLocation) View.VISIBLE else View.GONE
            chuInput.visibility = if (isLocation) View.VISIBLE else View.GONE
            facilityInput.visibility = if (isLocation) View.VISIBLE else View.GONE
            levelSpinner.visibility = if (isLocation) View.VISIBLE else View.GONE

            respondentTypeInput.visibility = if (isRespondent) View.VISIBLE else View.GONE
            respondentNameInput.visibility = if (isRespondent) View.VISIBLE else View.GONE

            commentsInput.visibility = if (isAssessment) View.VISIBLE else View.GONE
            pillarSpinner.visibility = if (isAssessment) View.VISIBLE else View.GONE
            questionKeyInput.visibility = if (isAssessment) View.VISIBLE else View.GONE
            questionValueInput.visibility = if (isAssessment) View.VISIBLE else View.GONE
            addQuestionBtn.visibility = if (isAssessment) View.VISIBLE else View.GONE
            pillarPreviewView.visibility = if (isAssessment) View.VISIBLE else View.GONE
            allPillarsPayloadInput.visibility = if (isAssessment) View.VISIBLE else View.GONE
            actionPlanBtn.visibility = if (isAssessment) View.VISIBLE else View.GONE
            recordStatusSpinner.visibility = if (isAssessment) View.VISIBLE else View.GONE

            prevBtn.visibility = if (sectionIndex == 0) View.INVISIBLE else View.VISIBLE
            nextBtn.visibility = if (sectionIndex == sections.lastIndex) View.INVISIBLE else View.VISIBLE
            sectionView.text = "Section: ${sections[sectionIndex]}"
            if (isLocation) {
                // Re-apply level-based rules so section toggles don't override questionnaire logic.
                applyLevelVisibility(levelSpinner.selectedItem?.toString() ?: draft.levelOfSupervision)
            }
        }

        fun validateDraft(d: SupervisionDraft): String? {
            if (d.county.isBlank()) return "County is required"
            if (d.levelOfSupervision != "county" && d.subCounty.isBlank()) return "Sub-county is required"
            if ((d.levelOfSupervision == "chu" || d.levelOfSupervision == "facility") && d.chu.isBlank()) {
                return "CHU is required"
            }
            if (d.levelOfSupervision == "facility" && d.facility.isBlank()) return "Facility is required"
            if (d.whoAreRespondents.isBlank()) return "Respondent type is required"
            if (d.respondentName.isBlank()) return "Respondent name is required"
            if (d.comments.length < 5) return "Assessment comments should be at least 5 characters"
            val payload = d.allPillarsPayloadJson.trim()
            if (payload.isNotBlank()) {
                try {
                    JSONObject(payload)
                } catch (_: Exception) {
                    return "All pillars payload must be valid JSON object"
                }
            }
            return null
        }

        fun bind(d: SupervisionDraft) {
            countyInput.setText(d.county)
            subCountyInput.setText(d.subCounty)
            chuInput.setText(d.chu)
            facilityInput.setText(d.facility)
            respondentTypeInput.setText(d.whoAreRespondents)
            respondentNameInput.setText(d.respondentName)
            commentsInput.setText(d.comments)
            allPillarsPayloadInput.setText(d.allPillarsPayloadJson)
            val idx = levels.indexOf(d.levelOfSupervision).coerceAtLeast(0)
            levelSpinner.setSelection(idx)
            val statusIdx = statuses.indexOf(d.recordStatus).coerceAtLeast(0)
            recordStatusSpinner.setSelection(statusIdx)
            statusView.text = "Draft ID: ${d.id} | Record: ${d.recordStatus} | Sync: ${d.syncStatus}"
            scoreView.text = "Readiness score: ${"%.1f".format(draftScore(d))}%"
            applyLevelVisibility(d.levelOfSupervision)
            applySectionVisibility()
            val currentPillar = pillarSpinner.selectedItem?.toString() ?: pillars.first()
            val preview = try {
                val root = JSONObject(d.allPillarsPayloadJson.ifBlank { "{}" })
                root.optJSONObject(currentPillar)?.toString(2) ?: "{}"
            } catch (_: Exception) {
                "{}"
            }
            pillarPreviewView.text = "Pillar preview ($currentPillar):\n$preview"
        }

        fun collect(): SupervisionDraft {
            val level = levelSpinner.selectedItem.toString()
            val respondentType = respondentTypeInput.text.toString().trim()
            val normalizedPayload = normalizeAllPillarsPayload(
                rawJson = allPillarsPayloadInput.text.toString(),
                levelOfSupervision = level,
                respondentType = respondentType
            )
            return draft.copy(
                county = countyInput.text.toString().trim(),
                subCounty = subCountyInput.text.toString().trim(),
                chu = chuInput.text.toString().trim(),
                facility = facilityInput.text.toString().trim(),
                levelOfSupervision = level,
                whoAreRespondents = respondentType,
                respondentName = respondentNameInput.text.toString().trim(),
                comments = commentsInput.text.toString().trim(),
                allPillarsPayloadJson = normalizedPayload,
                recordStatus = recordStatusSpinner.selectedItem.toString(),
                syncStatus = "draft",
                updatedAt = System.currentTimeMillis()
            )
        }

        bind(draft)

        fun refreshPillarPreview() {
            val currentPillar = pillarSpinner.selectedItem?.toString() ?: pillars.first()
            val preview = try {
                val root = JSONObject(allPillarsPayloadInput.text.toString().ifBlank { "{}" })
                root.optJSONObject(currentPillar)?.toString(2) ?: "{}"
            } catch (_: Exception) {
                "{}"
            }
            pillarPreviewView.text = "Pillar preview ($currentPillar):\n$preview"
        }

        fun upsertPillarQuestion() {
            val pillar = pillarSpinner.selectedItem?.toString() ?: return
            val questionKey = questionKeyInput.text.toString().trim()
            val questionValue = questionValueInput.text.toString().trim()
            if (questionKey.isBlank()) {
                Toast.makeText(this, "Enter question key", Toast.LENGTH_SHORT).show()
                return
            }
            val root = try {
                JSONObject(allPillarsPayloadInput.text.toString().ifBlank { "{}" })
            } catch (_: Exception) {
                JSONObject()
            }
            val pillarObj = (root.optJSONObject(pillar) ?: JSONObject())
            pillarObj.put(questionKey, questionValue)
            root.put(pillar, pillarObj)
            allPillarsPayloadInput.setText(root.toString())
            questionValueInput.setText("")
            refreshPillarPreview()
        }

        val usernameForSchema = session.getUsername()
        val passwordForSchema = session.getPassword()
        if (usernameForSchema.isNotBlank() && passwordForSchema.isNotBlank()) {
            lifecycleScope.launch {
                try {
                    val schema = withContext(Dispatchers.IO) {
                        SyncRepository(session.getBaseUrl()).questionnaireSchema(
                            usernameForSchema,
                            passwordForSchema
                        )
                    }
                    val previousVersion = schemaManager.getVersion()
                    val isUpdated = previousVersion.isNotBlank() && previousVersion != schema.schemaVersion
                    schemaManager.saveVersion(
                        version = schema.schemaVersion,
                        coverageOk = schema.allPillarsHaveAllLevels
                    )
                    val coverage = if (schema.allPillarsHaveAllLevels) "all-levels-covered" else "coverage-gap"
                    val marker = if (isUpdated) "updated" else "current"
                    schemaStatusView.text =
                        "Questionnaire schema: $marker | v=${schema.schemaVersion.take(10)} | $coverage"
                } catch (_: Exception) {
                    val cachedVersion = schemaManager.getVersion()
                    val cachedCoverage = if (schemaManager.getLastCoverageOk()) "all-levels-covered" else "coverage-unknown"
                    schemaStatusView.text = if (cachedVersion.isBlank()) {
                        "Questionnaire schema: offline (not yet synced)"
                    } else {
                        "Questionnaire schema: offline cache v=${cachedVersion.take(10)} | $cachedCoverage"
                    }
                }
            }
        } else {
            val cachedVersion = schemaManager.getVersion()
            val cachedCoverage = if (schemaManager.getLastCoverageOk()) "all-levels-covered" else "coverage-unknown"
            schemaStatusView.text = if (cachedVersion.isBlank()) {
                "Questionnaire schema: login to sync latest"
            } else {
                "Questionnaire schema: cached v=${cachedVersion.take(10)} | $cachedCoverage"
            }
        }

        levelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyLevelVisibility(levelSpinner.selectedItem.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        pillarSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshPillarPreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        addQuestionBtn.setOnClickListener { upsertPillarQuestion() }

        prevBtn.setOnClickListener {
            sectionIndex = (sectionIndex - 1).coerceAtLeast(0)
            applySectionVisibility()
        }

        nextBtn.setOnClickListener {
            sectionIndex = (sectionIndex + 1).coerceAtMost(sections.lastIndex)
            applySectionVisibility()
        }

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
            if (schemaManager.hasSchemaVersion() && !schemaManager.getLastCoverageOk()) {
                val message = "Questionnaire coverage gap detected. Sync latest schema before submitting."
                statusView.text = message
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            draft = collect()
            val validationError = validateDraft(draft)
            if (validationError != null) {
                Toast.makeText(this, validationError, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
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
                            deviceId = deviceIdProvider.getOrCreate(),
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
