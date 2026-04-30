package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserManagementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        val role = SessionManager(this).getRole()
        findViewById<TextView>(R.id.roleView).text = "Role: $role"

        findViewById<Button>(R.id.openWebUsersBtn).setOnClickListener {
            val intent = Intent(this, WebModuleActivity::class.java)
            intent.putExtra("path", "/users")
            intent.putExtra("title", "User Management (Web Fallback)")
            startActivity(intent)
        }
    }
}
