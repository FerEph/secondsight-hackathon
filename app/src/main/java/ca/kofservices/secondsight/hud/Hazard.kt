package ca.kofservices.secondsight.hud

/**
 * Hazard classes, audio-first: each class carries a distinct sound pattern
 * so the rider can tell classes apart without looking at the display.
 */
enum class HazardClass(
    val patternName: String,
    val toneDescription: String
) {
    OBSTACLE("double_low", "two short low tones"),
    PEDESTRIAN("soft_double_beep", "soft double beep, mid pitch"),
    ROAD_SIGN("chime", "single clear chime"),
    VEHICLE("triple_pulse", "three quick pulses, higher pitch")
}

/** One detected hazard, positioned relative to the rider. Advisory only. */
data class Hazard(
    val type: HazardClass,
    val direction: String, // e.g. "ahead, left"
    val distanceM: Double
)
