package ke.go.moh.supervision.mobile.data

import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionnairePayloadNormalizerTest {
    @Test
    fun normalizeAllPillarsPayload_includesAllPillarsAndMeta() {
        val normalized = normalizeAllPillarsPayload(
            rawJson = """{"workforce":{"q1":"yes"}}""",
            levelOfSupervision = "facility",
            respondentType = "CHP"
        )

        assertTrue(normalized.contains("\"workforce\""))
        assertTrue(normalized.contains("\"leadership\""))
        assertTrue(normalized.contains("\"monitoringandevaluation\""))
        assertTrue(normalized.contains("\"servicedelivery\""))
        assertTrue(normalized.contains("\"actionplan\""))
        assertTrue(normalized.contains("\"levelOfSupervision\":\"facility\""))
        assertTrue(normalized.contains("\"respondentType\":\"CHP\""))
    }
}
