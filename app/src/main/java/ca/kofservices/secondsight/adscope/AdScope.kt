package ca.kofservices.secondsight.adscope

/** One captured camera frame (reference only — pixels stay in the frame pipeline). */
data class AdFrame(
    val imageRef: String,
    val capturedAtMs: Long
)

/** One segmented region of a frame, weighted for the heatmap overlay. */
data class AdRegion(
    val label: String,
    val confidence: Float,
    val heatWeight: Float
)

/**
 * AdScope state.
 *
 * Locked UX rule: on the glasses the wearer sees ONLY the live viewfinder
 * plus the heatmap overlay — no buttons or controls on the HUD. All
 * controls, scores, recommendations and exports live on the phone.
 */
data class AdScopeState(
    val frame: AdFrame?,
    val regions: List<AdRegion>
)
