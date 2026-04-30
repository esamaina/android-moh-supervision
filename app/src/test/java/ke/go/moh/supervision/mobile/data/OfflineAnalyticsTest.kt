package ke.go.moh.supervision.mobile.data

import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineAnalyticsTest {
    @Test
    fun buildOfflineAnalytics_countsRecordsCorrectly() {
        val records = listOf(
            SupervisionDraft(
                id = "1",
                levelOfSupervision = "facility",
                whoAreRespondents = "In-charge",
                recordStatus = "completed",
                syncStatus = "synced",
                actionPlan = "Plan A"
            ),
            SupervisionDraft(
                id = "2",
                levelOfSupervision = "chu",
                whoAreRespondents = "CHO",
                recordStatus = "incomplete",
                syncStatus = "failed"
            ),
            SupervisionDraft(
                id = "3",
                levelOfSupervision = "facility",
                whoAreRespondents = "CHP",
                recordStatus = "completed",
                syncStatus = "draft"
            )
        )

        val analytics = buildOfflineAnalytics(records)
        assertEquals(3, analytics.total)
        assertEquals(2, analytics.completed)
        assertEquals(1, analytics.incomplete)
        assertEquals(1, analytics.synced)
        assertEquals(2, analytics.pendingSync)
        assertEquals(1, analytics.failedSync)
        assertEquals(2, analytics.byLevel["facility"])
        assertEquals(1, analytics.byLevel["chu"])
        assertEquals(1, analytics.withActionPlan)
    }
}
