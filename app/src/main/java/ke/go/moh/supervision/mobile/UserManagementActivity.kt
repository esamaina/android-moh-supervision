package ke.go.moh.supervision.mobile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ke.go.moh.supervision.mobile.data.MobileUser
import ke.go.moh.supervision.mobile.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserManagementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        val session = SessionManager(this)
        val role = session.getRole()
        val username = session.getUsername()
        val password = session.getPassword()
        val baseUrl = session.getBaseUrl()
        val listView = findViewById<ListView>(R.id.usersList)
        val loadBtn = findViewById<Button>(R.id.loadUsersBtn)

        findViewById<TextView>(R.id.roleView).text = "Role: $role"
        var users: List<MobileUser> = emptyList()
        var selected: MobileUser? = null

        fun render() {
            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                users.map { "${it.username} (${it.role}) - ${it.user_status}" }
            )
        }

        if (role !in listOf("admin", "national", "county", "subcounty")) {
            loadBtn.isEnabled = false
            Toast.makeText(this, "Role not permitted for native user management", Toast.LENGTH_LONG).show()
        }

        loadBtn.setOnClickListener {
            lifecycleScope.launch {
                try {
                    users = withContext(Dispatchers.IO) {
                        UserRepository(baseUrl).getUsers(username, password)
                    }
                    render()
                } catch (e: Exception) {
                    Toast.makeText(this@UserManagementActivity, "Load failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            selected = users.getOrNull(position)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val user = users.getOrNull(position) ?: return@setOnItemLongClickListener true
            AlertDialog.Builder(this)
                .setTitle("User action")
                .setItems(arrayOf("Toggle active/inactive", "Reset password to TempPass123")) { _, which ->
                    lifecycleScope.launch {
                        try {
                            val repo = UserRepository(baseUrl)
                            withContext(Dispatchers.IO) {
                                if (which == 0) {
                                    repo.toggleUserStatus(username, password, user)
                                } else {
                                    repo.changePassword(username, password, user.id, "TempPass123")
                                }
                            }
                            users = withContext(Dispatchers.IO) {
                                repo.getUsers(username, password)
                            }
                            render()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@UserManagementActivity,
                                "Action failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }

        findViewById<Button>(R.id.openWebUsersBtn).setOnClickListener {
            val intent = Intent(this, WebModuleActivity::class.java)
            intent.putExtra("path", "/users")
            intent.putExtra("title", "User Management (Web Fallback)")
            startActivity(intent)
        }
    }
}
