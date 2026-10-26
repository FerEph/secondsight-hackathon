package ca.kofservices.secondsight.voice

import ca.kofservices.secondsight.hud.Hazard
import ca.kofservices.secondsight.sensors.RiderState
import kotlin.math.roundToInt

/**
 * Builds spoken/displayed status text.
 * Factual readouts only — no advice, no recommendations, ever.
 */
object StatusReadout {

    fun format(state: RiderState, hazards: List<Hazard>): String {
        val parts = mutableListOf<String>()
        for (hazard in hazards) {
            parts += "${hazard.type.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }} " +
                "${hazard.direction}, ${hazard.distanceM.roundToInt()} metres."
        }
        parts += "Speed ${state.speedKmh.roundToInt()} kilometres per hour."
        state.heartRateBpm?.let { parts += "Heart rate $it beats per minute." }
        state.cadenceRpm?.let { parts += "Cadence $it revolutions per minute." }
        parts += "Distance ${"%.1f".format(state.distanceKm)} kilometres."
        return parts.joinToString(" ")
    }
}
