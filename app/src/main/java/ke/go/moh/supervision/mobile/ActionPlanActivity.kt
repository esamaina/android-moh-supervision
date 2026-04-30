package ke.go.moh.supervision.mobile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        if (draft == null) {
            titleView.text = "Action Plan (Draft not found)"
            saveBtn.isEnabled = false
            return
        }

        titleView.text = "Action Plan (${draft.id.take(8)})"
        planInput.setText(draft.actionPlan)
        dueDateInput.setText(draft.actionPlanDueDate)

        saveBtn.setOnClickListener {
            store.save(
                draft.copy(
                    actionPlan = planInput.text.toString().trim(),
                    actionPlanDueDate = dueDateInput.text.toString().trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            Toast.makeText(this, "Action plan saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
