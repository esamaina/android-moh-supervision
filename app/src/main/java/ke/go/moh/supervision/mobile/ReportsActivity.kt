package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore

class ReportsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        val records = SupervisionDraftStore(this).loadAll()
        val completed = records.count { it.recordStatus == "completed" }
        val incomplete = records.size - completed
        val syncPending = records.count { it.syncStatus != "synced" }

        findViewById<TextView>(R.id.summaryView).text =
            "Total records: ${records.size}\nCompleted: $completed\nIncomplete: $incomplete\nPending sync: $syncPending"

        findViewById<Button>(R.id.openWebReportsBtn).setOnClickListener {
            val intent = Intent(this, WebModuleActivity::class.java)
            intent.putExtra("path", "/scores")
            intent.putExtra("title", "Reports (Web Fallback)")
            startActivity(intent)
        }
    }
}
