package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val session = SessionManager(this)
        val isOfflineMode = session.getMode() == "offline"
        if (!session.hasCredentials()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        findViewById<Button>(R.id.supervisionBtn).setOnClickListener {
            if (isOfflineMode) {
                startActivity(Intent(this, SupervisionFormActivity::class.java))
            } else {
                startActivity(Intent(this, WebModuleActivity::class.java).apply {
                    putExtra("title", "New Supervision")
                    putExtra("path", "/new-supervision/${UUID.randomUUID()}")
                })
            }
        }
        findViewById<Button>(R.id.reportsBtn).setOnClickListener {
            startActivity(Intent(this, WebModuleActivity::class.java).apply {
                putExtra("title", "Dashboard")
                putExtra("path", "/dashboard")
            })
        }
        findViewById<Button>(R.id.dashboardBtn).setOnClickListener {
            if (isOfflineMode) {
                startActivity(Intent(this, DraftHistoryActivity::class.java))
            } else {
                startActivity(Intent(this, WebModuleActivity::class.java).apply {
                    putExtra("title", "Supervision Record")
                    putExtra("path", "/scores")
                })
            }
        }
        findViewById<Button>(R.id.usersBtn).setOnClickListener {
            startActivity(Intent(this, WebModuleActivity::class.java).apply {
                putExtra("title", "Users")
                putExtra("path", "/users")
            })
        }
        findViewById<Button>(R.id.syncBtn).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<Button>(R.id.historyBtn).setOnClickListener {
            startActivity(Intent(this, DraftHistoryActivity::class.java))
        }
        findViewById<Button>(R.id.usersBtn).visibility =
            if (session.getRole() == "admin") View.VISIBLE else View.GONE
        findViewById<Button>(R.id.logoutBtn).setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

}
