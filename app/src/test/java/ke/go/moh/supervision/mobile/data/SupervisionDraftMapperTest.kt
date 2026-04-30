package ke.go.moh.supervision.mobile.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupervisionDraftMapperTest {
    @Test
    fun draftToEntityAndBack_preservesCoreFields() {
        val draft = SupervisionDraft(
            id = "id-1",
            county = "Homa Bay",
            subCounty = "Homa Bay Town",
            chu = "CHU A",
            facility = "Facility A",
            levelOfSupervision = "facility",
            whoAreRespondents = "In-charge",
            respondentName = "Jane",
            comments = "All good",
            actionPlan = "Follow up next week",
            actionPlanDueDate = "2026-05-10",
            allPillarsPayloadJson = """{"workforce":{"q1":"yes"}}""",
            recordStatus = "completed",
            syncStatus = "draft",
            updatedAt = 123L
        )

        val restored = draft.toEntity().toDraft()
        assertEquals(draft.id, restored.id)
        assertEquals(draft.county, restored.county)
        assertEquals(draft.recordStatus, restored.recordStatus)
        assertEquals(draft.actionPlan, restored.actionPlan)
        assertTrue(restored.allPillarsPayloadJson.contains("workforce"))
    }
}
