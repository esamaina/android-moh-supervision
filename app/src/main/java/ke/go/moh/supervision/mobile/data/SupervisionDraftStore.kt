package ke.go.moh.supervision.mobile.data

import android.content.Context
import kotlinx.coroutines.runBlocking

class SupervisionDraftStore(context: Context) {
    private val repo = OfflineRecordRepository(context)

    fun save(draft: SupervisionDraft) {
        runBlocking { repo.save(draft) }
    }

    fun load(): SupervisionDraft {
        return loadAll().firstOrNull() ?: SupervisionDraft()
    }

    fun loadAll(): List<SupervisionDraft> {
        return runBlocking { repo.getAll() }
    }

    fun remove(id: String) {
        runBlocking { repo.remove(id) }
    }

    fun clear() {
        loadAll().forEach { remove(it.id) }
    }
}
