package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore
import ke.go.moh.supervision.mobile.data.buildOfflineAnalytics

class ReportsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        val summaryView = findViewById<TextView>(R.id.summaryView)
        val detailView = findViewById<TextView>(R.id.detailView)
        val refreshBtn = findViewById<Button>(R.id.refreshBtn)

        fun render() {
            val analytics = buildOfflineAnalytics(SupervisionDraftStore(this).loadAll())
            summaryView.text =
                "Total records: ${analytics.total}\nCompleted: ${analytics.completed}\nIncomplete: ${analytics.incomplete}\nSynced: ${analytics.synced}\nPending sync: ${analytics.pendingSync}\nFailed sync: ${analytics.failedSync}\nAction plans: ${analytics.withActionPlan}"
            detailView.text =
                "By Level:\n" + analytics.byLevel.entries.joinToString("\n") { "${it.key}: ${it.value}" } +
                    "\n\nBy Respondent:\n" +
                    analytics.byRespondent.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        }
        render()
        refreshBtn.setOnClickListener { render() }

        findViewById<Button>(R.id.openWebReportsBtn).setOnClickListener {
            val intent = Intent(this, WebModuleActivity::class.java)
            intent.putExtra("path", "/scores")
            intent.putExtra("title", "Reports (Web Fallback)")
            startActivity(intent)
        }
    }
}
