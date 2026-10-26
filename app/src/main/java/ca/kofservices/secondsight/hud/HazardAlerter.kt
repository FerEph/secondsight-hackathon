package ca.kofservices.secondsight.hud

import android.media.AudioManager
import android.media.ToneGenerator

/** Plays the distinct sound pattern for a hazard. Behind an interface for tests. */
interface HazardAlerter {
    fun alert(hazard: Hazard)
}

/** Maps each hazard class to its pattern descriptor from [HazardClass]. */
class SoundPatternMapper {
    fun patternFor(hazard: Hazard): String = hazard.type.patternName
    fun descriptionFor(hazard: Hazard): String = hazard.type.toneDescription
}

/**
 * Android sketch: short ToneGenerator beeps shaped by the class pattern.
 * Exact tone sequences are tuned on-device during the event.
 */
class ToneHazardAlerter : HazardAlerter {
    private val tones = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)

    override fun alert(hazard: Hazard) {
        // TODO(hackathon): per-pattern timing/duration; scaffold plays one beep.
        when (hazard.type) {
            HazardClass.OBSTACLE -> tones.startTone(ToneGenerator.TONE_PROP_BEEP, 180)
            HazardClass.PEDESTRIAN -> tones.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
            HazardClass.ROAD_SIGN -> tones.startTone(ToneGenerator.TONE_PROP_ACK, 150)
            HazardClass.VEHICLE -> tones.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        }
    }
}
