package ca.kofservices.secondsight.auditor

/** The KOF 10-dimension governance risk rubric for AI agents. */
enum class RubricDimension(val label: String) {
    AUTONOMY_BOUNDS("Autonomy bounds"),
    TERMINATION_AND_BUDGET_LIMITS("Termination and budget limits"),
    ACCOUNTABILITY("Accountability"),
    MEMORY_GOVERNANCE("Memory governance"),
    OBSERVABILITY("Observability"),
    ACCEPTANCE_GATES("Acceptance gates"),
    STATE_OWNERSHIP("State ownership"),
    TRIGGER_HYGIENE("Trigger hygiene"),
    POLICY_VERSIONING("Policy versioning"),
    DATA_BOUNDARIES("Data boundaries")
}

data class DimensionScore(
    val dimension: RubricDimension,
    val score: Int, // 0–5 once evidence is assessed; 0 in this scaffold
    val finding: String
)

/**
 * Deterministic placeholder scorer for the scaffold: every dimension scores 0
 * and is flagged as having no evidence yet. The conversational layer via the
 * Meta Model API — which gathers evidence dimension by dimension — is wired
 * in later in the event and replaces this scorer.
 */
object PlaceholderAuditorScorer {
    fun scoreAll(): List<DimensionScore> =
        RubricDimension.entries.map { dimension ->
            DimensionScore(dimension, 0, "No evidence provided yet for ${dimension.label}.")
        }
}
