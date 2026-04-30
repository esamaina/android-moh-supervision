package ke.go.moh.supervision.mobile.sync

import ke.go.moh.supervision.mobile.data.SupervisionDraft

internal fun shouldSkipPushByPolicy(draft: SupervisionDraft): Boolean {
    return draft.conflictPolicy == "server_wins"
}

internal fun draftAfterSyncSuccess(draft: SupervisionDraft, nowMs: Long): SupervisionDraft {
    return draft.copy(
        syncStatus = "synced",
        conflictPolicy = "none",
        updatedAt = nowMs
    )
}

internal fun draftAfterSyncFailure(draft: SupervisionDraft, nowMs: Long): SupervisionDraft {
    val nextPolicy = if (draft.conflictPolicy == "none") "server_wins" else draft.conflictPolicy
    return draft.copy(
        syncStatus = "failed",
        conflictPolicy = nextPolicy,
        updatedAt = nowMs
    )
}
