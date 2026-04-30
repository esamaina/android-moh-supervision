package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val session = SessionManager(this)
        val baseUrlInput = findViewById<EditText>(R.id.baseUrlInput)
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val roleSpinner = findViewById<Spinner>(R.id.roleSpinner)
        val roles = listOf("user", "admin")
        roleSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        baseUrlInput.setText(session.getBaseUrl())
        usernameInput.setText(session.getUsername())
        passwordInput.setText(session.getPassword())
        roleSpinner.setSelection(roles.indexOf(session.getRole()).coerceAtLeast(0))

        findViewById<Button>(R.id.signInBtn).setOnClickListener {
            val baseUrl = baseUrlInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (baseUrl.isBlank() || username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Enter base URL, username and password", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            session.saveBaseUrl(baseUrl)
            session.saveUsername(username)
            session.savePassword(password)
            session.saveRole(roleSpinner.selectedItem.toString())

            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
    }
}
