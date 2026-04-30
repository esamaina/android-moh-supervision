package ke.go.moh.supervision.mobile.data

import android.content.Context

class QuestionnaireSchemaManager(context: Context) {
    private val prefs = context.getSharedPreferences("sst_questionnaire_schema", Context.MODE_PRIVATE)

    fun getVersion(): String = prefs.getString(KEY_SCHEMA_VERSION, "") ?: ""
    fun hasSchemaVersion(): Boolean = getVersion().isNotBlank()
    fun getLastSyncAt(): Long = prefs.getLong(KEY_SCHEMA_SYNC_AT, 0L)
    fun getLastCoverageOk(): Boolean = prefs.getBoolean(KEY_SCHEMA_COVERAGE_OK, false)

    fun saveVersion(version: String, coverageOk: Boolean? = null) {
        if (version.isBlank()) return
        prefs.edit()
            .putString(KEY_SCHEMA_VERSION, version)
            .putLong(KEY_SCHEMA_SYNC_AT, System.currentTimeMillis())
            .apply()
        if (coverageOk != null) {
            prefs.edit().putBoolean(KEY_SCHEMA_COVERAGE_OK, coverageOk).apply()
        }
    }

    companion object {
        private const val KEY_SCHEMA_VERSION = "schema_version"
        private const val KEY_SCHEMA_SYNC_AT = "schema_sync_at"
        private const val KEY_SCHEMA_COVERAGE_OK = "schema_coverage_ok"
    }
}
