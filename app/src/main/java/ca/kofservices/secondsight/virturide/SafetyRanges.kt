package ca.kofservices.secondsight.virturide

import ca.kofservices.secondsight.sensors.RiderState

/**
 * Safety Ranges — user-set monitoring brackets per signal, monitoring
 * only: the HUD tints as a value nears its limit and speaks one plain
 * factual alert per over-limit crossing. No zone names, no coaching,
 * no advice.
 *
 * Ported (Nov 2, 2026) from the VirtuRide Safety Ranges logic in the
 * private kof-glass-app repo (feature/v17.11). Only the pure evaluation
 * is ported here: thresholds, state words, colours and the one-shot
 * alert arming. The private UI (range editor cards, TTS wiring) is not
 * ported; this repo renders the report as text through MainActivity.
 *
 * Defaults match the private build: heart rate caution 140 / max 170
 * bpm, speed caution 45 / max 60 km/h, VOx caution 70 / alert below 50.
 * VOx is low-side (it warns when it drops), unlike heart rate and speed.
 */
enum class RangeState {
    IN_RANGE,
    NEAR_LIMIT,
    OVER_LIMIT
}

/** High-side bracket: warn as the value climbs toward [max]. */
data class SafetyRange(val caution: Int, val max: Int)

/**
 * All brackets. Construct with defaults, or pass edited values; the
 * private editor's validation bounds were HR caution 60–200 / max
 * caution+5–230, speed caution 5–100 / max caution+5–140, VOx caution
 * 40–95 / alert 10–(caution-5).
 */
data class SafetyRanges(
    val heartRate: SafetyRange = SafetyRange(caution = 140, max = 170),
    val speed: SafetyRange = SafetyRange(caution = 45, max = 60),
    val voxCaution: Int = 70,
    val voxLow: Int = 50
)

fun evaluateHighSide(value: Double, range: SafetyRange): RangeState = when {
    value > range.max -> RangeState.OVER_LIMIT
    value >= range.caution -> RangeState.NEAR_LIMIT
    else -> RangeState.IN_RANGE
}

/** Low-side VOx evaluation: a lower score is worse. */
fun evaluateVox(score: Int, ranges: SafetyRanges): RangeState = when {
    score < ranges.voxLow -> RangeState.OVER_LIMIT
    score < ranges.voxCaution -> RangeState.NEAR_LIMIT
    else -> RangeState.IN_RANGE
}

fun rangeWord(state: RangeState): String = when (state) {
    RangeState.OVER_LIMIT -> "OVER LIMIT"
    RangeState.NEAR_LIMIT -> "NEAR LIMIT"
    RangeState.IN_RANGE -> "IN RANGE"
}

/** Same ARGB tints as the private build: green / amber / red. */
fun rangeColor(state: RangeState): Int = when (state) {
    RangeState.OVER_LIMIT -> 0xFFFA383E.toInt()
    RangeState.NEAR_LIMIT -> 0xFFF5C33B.toInt()
    RangeState.IN_RANGE -> 0xFF31A24C.toInt()
}

/**
 * One-shot over-limit alerts, mirroring the private 1-second tick:
 * each signal alerts at most once per crossing (re-armed when it
 * returns to IN_RANGE for heart rate / speed, or to at/above caution
 * for VOx), with a shared 5-second gap between any two alerts.
 * Phrases are the private build's factual alerts, verbatim.
 */
class SafetyRangeMonitor(private val ranges: SafetyRanges = SafetyRanges()) {

    private var hrOverArmed = true
    private var spdOverArmed = true
    private var voxOverArmed = true
    private var lastAlertMs = 0L

    /** Factual over-limit phrases for this snapshot, if any are newly due. */
    fun overLimitAlerts(state: RiderState, voxScore: Int? = null): List<String> {
        val now = System.currentTimeMillis()
        val alerts = mutableListOf<String>()
        fun due(): Boolean = now - lastAlertMs > 5000

        state.heartRateBpm?.let { hr ->
            val st = evaluateHighSide(hr.toDouble(), ranges.heartRate)
            if (st == RangeState.OVER_LIMIT && hrOverArmed && due()) {
                hrOverArmed = false
                lastAlertMs = now
                alerts += "Heart rate over your safety range"
            } else if (st == RangeState.IN_RANGE) {
                hrOverArmed = true
            }
        }
        val spdState = evaluateHighSide(state.speedKmh, ranges.speed)
        if (spdState == RangeState.OVER_LIMIT && spdOverArmed && due()) {
            spdOverArmed = false
            lastAlertMs = now
            alerts += "Speed over your safety range"
        } else if (spdState == RangeState.IN_RANGE) {
            spdOverArmed = true
        }
        voxScore?.let { vox ->
            val st = evaluateVox(vox, ranges)
            if (st == RangeState.OVER_LIMIT && voxOverArmed && due()) {
                voxOverArmed = false
                lastAlertMs = now
                alerts += "VOx below your safety range"
            } else if (vox >= ranges.voxCaution) {
                voxOverArmed = true
            }
        }
        return alerts
    }

    /** One-line range report in the private build's format. */
    fun report(state: RiderState, voxScore: Int? = null): String {
        val hrText = state.heartRateBpm?.let { hr ->
            "$hr — ${rangeWord(evaluateHighSide(hr.toDouble(), ranges.heartRate))}"
        } ?: "—"
        val spdText = "${state.speedKmh.toInt()} — " +
            rangeWord(evaluateHighSide(state.speedKmh, ranges.speed))
        val voxText = voxScore?.let { vox ->
            " · VOX $vox — ${rangeWord(evaluateVox(vox, ranges))}"
        } ?: ""
        return "HR $hrText · SPEED $spdText$voxText"
    }
}
