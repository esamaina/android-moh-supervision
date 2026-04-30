package ke.go.moh.supervision.mobile.data

import org.json.JSONObject

private val REQUIRED_PILLARS = listOf(
    "leadership",
    "workforce",
    "infrastructure",
    "monitoringandevaluation",
    "commodities",
    "transport",
    "referral",
    "finance",
    "partnership",
    "pandemic",
    "supervision",
    "servicedelivery",
    "actionplan"
)

fun normalizeAllPillarsPayload(
    rawJson: String,
    levelOfSupervision: String,
    respondentType: String
): String {
    val root = try {
        val raw = rawJson.trim()
        if (raw.isEmpty()) JSONObject() else JSONObject(raw)
    } catch (_: Exception) {
        JSONObject()
    }

    REQUIRED_PILLARS.forEach { pillar ->
        if (!root.has(pillar) || root.opt(pillar) !is JSONObject) {
            root.put(pillar, JSONObject())
        }
    }

    val meta = (root.opt("meta") as? JSONObject) ?: JSONObject()
    meta.put("levelOfSupervision", levelOfSupervision)
    meta.put("respondentType", respondentType)
    root.put("meta", meta)

    return root.toString()
}
