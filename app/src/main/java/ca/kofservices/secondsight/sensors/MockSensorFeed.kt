package ca.kofservices.secondsight.sensors

/**
 * Deterministic synthetic feed for desk development.
 * All values below are made-up test data, cycled in a fixed order —
 * they are not recorded from any real ride or person.
 */
class MockSensorFeed : SensorFeed {

    private val sequence = listOf(
        RiderState(0.0, 72, 0, 45.0, 18.0, 0.0),
        RiderState(12.5, 96, 62, 46.2, 18.0, 0.4),
        RiderState(24.0, 118, 78, 48.5, 18.5, 1.1),
        RiderState(32.0, 128, 84, 51.0, 19.0, 2.0),
        RiderState(28.5, 124, 80, 53.4, 19.0, 2.8),
        RiderState(18.0, 110, 70, 55.1, 19.5, 3.5)
    )

    private var index = 0

    override fun next(): RiderState {
        val state = sequence[index % sequence.size]
        index++
        return state
    }
}
