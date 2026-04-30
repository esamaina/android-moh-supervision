package ke.go.moh.supervision.mobile.sync

import android.content.Context
import android.provider.Settings

class DeviceIdProvider(private val context: Context) {
    private val prefs = context.getSharedPreferences("sst_device", Context.MODE_PRIVATE)

    fun getOrCreate(): String {
        val saved = prefs.getString(KEY_DEVICE_ID, "") ?: ""
        if (saved.isNotBlank()) return saved

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val generated = if (androidId.isNullOrBlank()) {
            "android-${System.currentTimeMillis()}"
        } else {
            "android-$androidId"
        }
        prefs.edit().putString(KEY_DEVICE_ID, generated).apply()
        return generated
    }

    fun save(deviceId: String) {
        if (deviceId.isBlank()) return
        prefs.edit().putString(KEY_DEVICE_ID, deviceId.trim()).apply()
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
    }
}
