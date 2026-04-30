package ke.go.moh.supervision.mobile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ke.go.moh.supervision.mobile.data.SupervisionDraft
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore
import ke.go.moh.supervision.mobile.data.SyncRepository
import ke.go.moh.supervision.mobile.sync.DeviceIdProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DraftHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_history)

        val session = SessionManager(this)
        val deviceIdProvider = DeviceIdProvider(this)
        val store = SupervisionDraftStore(this)
        val listView = findViewById<ListView>(R.id.draftList)
        val syncAllBtn = findViewById<Button>(R.id.syncAllBtn)
        val refreshBtn = findViewById<Button>(R.id.refreshBtn)
        val syncSelectedBtn = findViewById<Button>(R.id.syncSelectedBtn)
        val toggleStatusBtn = findViewById<Button>(R.id.toggleStatusBtn)
        val deleteSelectedBtn = findViewById<Button>(R.id.deleteFirstBtn)
        val conflictBtn = findViewById<Button>(R.id.resolveConflictBtn)
        val filterSpinner = findViewById<Spinner>(R.id.statusFilterSpinner)
        var selectedDraftId: String? = null
        var currentItems = emptyList<SupervisionDraft>()
        val filters = listOf("all", "completed", "incomplete")
        filterSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)

        fun render(items: List<SupervisionDraft>, filter: String) {
            currentItems = if (filter == "all") items else items.filter { it.recordStatus == filter }
            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                currentItems.map {
                    "${it.levelOfSupervision.uppercase()} | ${it.county}/${it.subCounty} | record:${it.recordStatus} | sync:${it.syncStatus} | conflict:${it.conflictPolicy} | ${it.id.take(8)}"
                }
            )
        }

        fun refresh() = render(store.loadAll(), filterSpinner.selectedItem?.toString() ?: "all")
        refresh()

        listView.setOnItemClickListener { _, _, position, _ ->
            val item = currentItems.getOrNull(position) ?: return@setOnItemClickListener
            selectedDraftId = item.id
            val intent = Intent(this, SupervisionFormActivity::class.java)
            intent.putExtra("draftId", item.id)
            startActivity(intent)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val item = currentItems.getOrNull(position) ?: return@setOnItemLongClickListener true
            AlertDialog.Builder(this)
                .setTitle("Delete draft?")
                .setMessage("Delete selected draft ${item.id.take(8)}?")
                .setPositiveButton("Delete") { _, _ ->
                    store.remove(item.id)
                    refresh()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }

        refreshBtn.setOnClickListener { refresh() }
        filterSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                refresh()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        toggleStatusBtn.setOnClickListener {
            val draftId = selectedDraftId
            if (draftId == null) {
                Toast.makeText(this, "Tap a record first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val draft = store.loadAll().firstOrNull { it.id == draftId } ?: return@setOnClickListener
            val nextStatus = if (draft.recordStatus == "completed") "incomplete" else "completed"
            store.save(draft.copy(recordStatus = nextStatus, syncStatus = "draft", updatedAt = System.currentTimeMillis()))
            refresh()
        }

        conflictBtn.setOnClickListener {
            val draftId = selectedDraftId
            if (draftId == null) {
                Toast.makeText(this, "Tap a record first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val draft = store.loadAll().firstOrNull { it.id == draftId } ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Resolve conflict")
                .setItems(arrayOf("Server wins", "Local wins", "Clear policy")) { _, which ->
                    val policy = when (which) {
                        0 -> "server_wins"
                        1 -> "local_wins"
                        else -> "none"
                    }
                    val nextSync = if (policy == "none") draft.syncStatus else "draft"
                    store.save(
                        draft.copy(
                            conflictPolicy = policy,
                            syncStatus = nextSync,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    refresh()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        deleteSelectedBtn.setOnClickListener {
            val draftId = selectedDraftId
            if (draftId == null) {
                Toast.makeText(this, "Tap a record first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val draft = store.loadAll().firstOrNull { it.id == draftId } ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Delete record?")
                .setMessage("Delete ${draft.id.take(8)} permanently from offline storage?")
                .setPositiveButton("Delete") { _, _ ->
                    store.remove(draft.id)
                    selectedDraftId = null
                    refresh()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        syncSelectedBtn.setOnClickListener {
            val draftId = selectedDraftId
            if (draftId == null) {
                Toast.makeText(this, "Tap a draft first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val draft = store.loadAll().firstOrNull { it.id == draftId }
            if (draft == null) {
                Toast.makeText(this, "Draft not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val username = session.getUsername()
            val password = session.getPassword()
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        SyncRepository(session.getBaseUrl()).pushDraft(
                            username, password, deviceIdProvider.getOrCreate(), draft
                        )
                    }
                    store.save(draft.copy(syncStatus = "synced", updatedAt = System.currentTimeMillis()))
                } catch (_: Exception) {
                    store.save(draft.copy(syncStatus = "failed", updatedAt = System.currentTimeMillis()))
                } finally {
                    refresh()
                }
            }
        }

        syncAllBtn.setOnClickListener {
            val username = session.getUsername()
            val password = session.getPassword()
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val drafts = store.loadAll()
            if (drafts.isEmpty()) {
                Toast.makeText(this, "No local drafts", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            syncAllBtn.isEnabled = false
            lifecycleScope.launch {
                val repo = SyncRepository(session.getBaseUrl())
                var success = 0
                for (draft in drafts) {
                    try {
                        withContext(Dispatchers.IO) {
                            repo.pushDraft(username, password, deviceIdProvider.getOrCreate(), draft)
                        }
                        store.save(
                            draft.copy(
                                syncStatus = "synced",
                                conflictPolicy = "none",
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        success++
                    } catch (_: Exception) {
                        store.save(
                            draft.copy(
                                syncStatus = "failed",
                                conflictPolicy = if (draft.conflictPolicy == "none") "server_wins" else draft.conflictPolicy,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
                refresh()
                syncAllBtn.isEnabled = true
                Toast.makeText(
                    this@DraftHistoryActivity,
                    "Synced $success of ${drafts.size} drafts",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
