package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore

class ActionPlanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_plan)

        val draftId = intent.getStringExtra("draftId")
        val store = SupervisionDraftStore(this)
        val draft = draftId?.let { id -> store.loadAll().firstOrNull { it.id == id } }

        val titleView = findViewById<TextView>(R.id.titleView)
        val planInput = findViewById<EditText>(R.id.planInput)
        val dueDateInput = findViewById<EditText>(R.id.dueDateInput)
        val openWebBtn = findViewById<Button>(R.id.saveBtn)

        if (draft == null) {
            titleView.text = "Action Plan (Draft not found)"
            openWebBtn.isEnabled = false
            return
        }

        titleView.text = "Action Plan Report (${draft.id.take(8)})"
        if (draft.actionPlan.isBlank() && draft.actionPlanDueDate.isBlank()) {
            planInput.setText("No action plan report available offline. Create it in the web app.")
            dueDateInput.setText("N/A")
        } else {
            planInput.setText(draft.actionPlan)
            dueDateInput.setText(draft.actionPlanDueDate.ifBlank { "N/A" })
        }
        planInput.isEnabled = false
        dueDateInput.isEnabled = false

        openWebBtn.setOnClickListener {
            startActivity(Intent(this, WebModuleActivity::class.java).apply {
                putExtra("title", "Action Plan Report")
                putExtra("path", "/scores")
            })
        }
    }
}
