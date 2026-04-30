package ke.go.moh.supervision.mobile.sync

import ke.go.moh.supervision.mobile.data.SupervisionDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncConflictPolicyTest {
    @Test
    fun shouldSkipPushOnlyWhenServerWins() {
        assertTrue(shouldSkipPushByPolicy(SupervisionDraft(conflictPolicy = "server_wins")))
        assertFalse(shouldSkipPushByPolicy(SupervisionDraft(conflictPolicy = "local_wins")))
        assertFalse(shouldSkipPushByPolicy(SupervisionDraft(conflictPolicy = "none")))
    }

    @Test
    fun successAlwaysClearsConflictAndMarksSynced() {
        val now = 1234L
        val next = draftAfterSyncSuccess(
            SupervisionDraft(syncStatus = "failed", conflictPolicy = "local_wins"),
            now
        )
        assertEquals("synced", next.syncStatus)
        assertEquals("none", next.conflictPolicy)
        assertEquals(now, next.updatedAt)
    }

    @Test
    fun failureDefaultsNonePolicyToServerWins() {
        val now = 5678L
        val next = draftAfterSyncFailure(SupervisionDraft(conflictPolicy = "none"), now)
        assertEquals("failed", next.syncStatus)
        assertEquals("server_wins", next.conflictPolicy)
        assertEquals(now, next.updatedAt)
    }

    @Test
    fun failurePreservesExplicitPolicy() {
        val localWins = draftAfterSyncFailure(SupervisionDraft(conflictPolicy = "local_wins"), 42L)
        val serverWins = draftAfterSyncFailure(SupervisionDraft(conflictPolicy = "server_wins"), 42L)
        assertEquals("local_wins", localWins.conflictPolicy)
        assertEquals("server_wins", serverWins.conflictPolicy)
    }
}
