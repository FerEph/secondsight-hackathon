package ca.kofservices.secondsight.sensors

/** One factual snapshot of rider state. Heart rate is a raw readout only. */
data class RiderState(
    val speedKmh: Double,
    val heartRateBpm: Int?,
    val cadenceRpm: Int?,
    val elevationM: Double,
    val temperatureC: Double,
    val distanceKm: Double
)

/** Source of rider-state snapshots: BLE sensors, GPS fusion, or a mock feed. */
interface SensorFeed {
    fun next(): RiderState
}
