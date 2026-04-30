package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ke.go.moh.supervision.mobile.data.SupervisionDraftStore

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val data = SupervisionDraftStore(this).loadAll()
        val byLevel = data.groupingBy { it.levelOfSupervision }.eachCount()
        findViewById<TextView>(R.id.metricsView).text =
            byLevel.entries.joinToString("\n") { (k, v) -> "$k: $v" }.ifBlank { "No records yet." }

        findViewById<Button>(R.id.openWebDashboardBtn).setOnClickListener {
            val intent = Intent(this, WebModuleActivity::class.java)
            intent.putExtra("path", "/dashboard")
            intent.putExtra("title", "Dashboard (Web Fallback)")
            startActivity(intent)
        }
    }
}
