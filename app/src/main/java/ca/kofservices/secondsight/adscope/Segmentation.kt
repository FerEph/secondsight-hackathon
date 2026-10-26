package ca.kofservices.secondsight.adscope

/** Segments advertising/signage regions out of a captured frame. */
interface SegmentationEngine {
    fun segment(frame: AdFrame): List<AdRegion>
}

// TODO(hackathon): wire the Meta Model API SAM 3.1 implementation behind
// this interface during the event. The placeholder below is the scaffold
// stand-in and segments nothing.

/** Placeholder engine: returns no regions until the SAM 3.1 wiring lands. */
class PlaceholderSegmentationEngine : SegmentationEngine {
    override fun segment(frame: AdFrame): List<AdRegion> = emptyList()
}
