package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val session = SessionManager(this)
        if (!session.hasCredentials()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        findViewById<Button>(R.id.supervisionBtn).setOnClickListener {
            startActivity(Intent(this, SupervisionFormActivity::class.java))
        }
        findViewById<Button>(R.id.reportsBtn).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        findViewById<Button>(R.id.dashboardBtn).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        findViewById<Button>(R.id.usersBtn).setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
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
