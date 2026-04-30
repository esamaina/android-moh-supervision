package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore
import ke.go.moh.supervision.mobile.data.buildOfflineAnalytics

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val metricsView = findViewById<TextView>(R.id.metricsView)
        val healthView = findViewById<TextView>(R.id.healthView)
        val refreshBtn = findViewById<Button>(R.id.refreshBtn)

        fun render() {
            val analytics = buildOfflineAnalytics(SupervisionDraftStore(this).loadAll())
            metricsView.text =
                analytics.byLevel.entries.joinToString("\n") { (k, v) -> "$k: $v" }
                    .ifBlank { "No records yet." }
            healthView.text =
                "Sync health\nPending: ${analytics.pendingSync}\nFailed: ${analytics.failedSync}\nSynced: ${analytics.synced}\nAction plans: ${analytics.withActionPlan}"
        }
        render()
        refreshBtn.setOnClickListener { render() }

        findViewById<Button>(R.id.openWebDashboardBtn).setOnClickListener {
            val intent = Intent(this, WebModuleActivity::class.java)
            intent.putExtra("path", "/dashboard")
            intent.putExtra("title", "Dashboard (Web Fallback)")
            startActivity(intent)
        }
    }
}
