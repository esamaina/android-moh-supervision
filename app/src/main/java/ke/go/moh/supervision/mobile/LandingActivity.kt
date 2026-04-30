package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ke.go.moh.supervision.mobile.sync.SyncScheduler

class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        SyncScheduler.schedule(this)

        findViewById<Button>(R.id.getStartedBtn).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
