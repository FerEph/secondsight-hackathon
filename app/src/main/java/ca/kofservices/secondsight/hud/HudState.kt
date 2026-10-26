package ca.kofservices.secondsight.hud

import ca.kofservices.secondsight.sensors.RiderState

/** Everything one HUD frame needs: rider readouts, active hazards, hero metric. */
data class HudState(
    val rider: RiderState,
    val hazards: List<Hazard>,
    val heroMetric: String
)

/**
 * Renders a [HudState]. The Meta Wearables DAT display implementation will
 * render this state on the glasses (view + HUD only, no controls); the
 * scaffold uses a simple on-phone implementation in MainActivity.
 */
interface HudRenderer {
    fun render(state: HudState)
}
