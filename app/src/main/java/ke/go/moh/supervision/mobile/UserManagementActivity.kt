package ke.go.moh.supervision.mobile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
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
        val createBtn = findViewById<Button>(R.id.createUserBtn)
        val statusView = findViewById<TextView>(R.id.statusView)

        findViewById<TextView>(R.id.roleView).text = "Role: $role"
        statusView.text = "Status: ready"
        var users: List<MobileUser> = emptyList()

        fun render() {
            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                users.map { "${it.username} (${it.role}) - ${it.user_status}" }
            )
        }

        if (role !in listOf("admin", "national", "county", "subcounty")) {
            loadBtn.isEnabled = false
            createBtn.isEnabled = false
            statusView.text = "Status: role not permitted for native user management"
            Toast.makeText(this, "Role not permitted for native user management", Toast.LENGTH_LONG).show()
        }

        loadBtn.setOnClickListener {
            loadBtn.isEnabled = false
            createBtn.isEnabled = false
            statusView.text = "Status: loading users..."
            lifecycleScope.launch {
                try {
                    users = withContext(Dispatchers.IO) {
                        UserRepository(baseUrl).getUsers(username, password)
                    }
                    render()
                    statusView.text = "Status: loaded ${users.size} users"
                } catch (e: Exception) {
                    statusView.text = "Status: load failed"
                    Toast.makeText(this@UserManagementActivity, "Load failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    if (role in listOf("admin", "national", "county", "subcounty")) {
                        loadBtn.isEnabled = true
                        createBtn.isEnabled = true
                    }
                }
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            users.getOrNull(position)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val user = users.getOrNull(position) ?: return@setOnItemLongClickListener true
            AlertDialog.Builder(this)
                .setTitle("User action")
                .setItems(arrayOf("Toggle active/inactive", "Reset password to TempPass123")) { _, which ->
                    statusView.text = "Status: applying user action..."
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
                            statusView.text = "Status: user action applied"
                        } catch (e: Exception) {
                            statusView.text = "Status: user action failed"
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

        createBtn.setOnClickListener {
            val emailInput = EditText(this).apply { hint = "Email" }
            val usernameInput = EditText(this).apply { hint = "Username" }
            val passwordInput = EditText(this).apply { hint = "Password (min 8 chars)" }
            val roleInput = EditText(this).apply { hint = "Role (cha/subcounty/county/national/admin)" }
            val container = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(40, 20, 40, 0)
                addView(emailInput)
                addView(usernameInput)
                addView(passwordInput)
                addView(roleInput)
            }
            AlertDialog.Builder(this)
                .setTitle("Create user")
                .setView(container)
                .setPositiveButton("Create") { _, _ ->
                    statusView.text = "Status: creating user..."
                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                UserRepository(baseUrl).createUser(
                                    username = username,
                                    password = password,
                                    email = emailInput.text.toString().trim(),
                                    newUsername = usernameInput.text.toString().trim(),
                                    newPassword = passwordInput.text.toString(),
                                    role = roleInput.text.toString().trim().ifBlank { "cha" }
                                )
                            }
                            users = withContext(Dispatchers.IO) {
                                UserRepository(baseUrl).getUsers(username, password)
                            }
                            render()
                            statusView.text = "Status: user created"
                            Toast.makeText(this@UserManagementActivity, "User created", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            statusView.text = "Status: create failed"
                            Toast.makeText(this@UserManagementActivity, "Create failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<Button>(R.id.openWebUsersBtn).setOnClickListener {
            val intent = Intent(this, WebModuleActivity::class.java)
            intent.putExtra("path", "/users")
            intent.putExtra("title", "User Management (Web Fallback)")
            startActivity(intent)
        }
    }
}
