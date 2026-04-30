package ke.go.moh.supervision.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import ke.go.moh.supervision.mobile.sync.SyncScheduler

class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        SyncScheduler.schedule(this)
        val session = SessionManager(this)
        val modeGroup = findViewById<RadioGroup>(R.id.modeGroup)
        if (session.getMode() == "offline") {
            modeGroup.check(R.id.modeOffline)
        } else {
            modeGroup.check(R.id.modeOnline)
        }

        findViewById<Button>(R.id.getStartedBtn).setOnClickListener {
            val selectedMode = if (modeGroup.checkedRadioButtonId == R.id.modeOffline) "offline" else "online"
            session.setMode(selectedMode)
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
