package ca.kofservices.secondsight.virturide

import android.content.Context
import ca.kofservices.secondsight.sensors.RiderState

/**
 * VirtuRide ride mode for the hackathon repo: the shell-wiring
 * counterpart to the private build's VIRTURIDE tab.
 *
 * What it owns: ride start/stop, the [GpsTracker] feed, and
 * [SafetyRangeMonitor] evaluation. A ride snapshot keeps heart rate,
 * cadence and temperature from the current sensor feed (BLE or mock)
 * and replaces speed, elevation and distance with live GPS values.
 *
 * What it deliberately does NOT do (these stay private, because they
 * depend on the private glasses session / ViewModel and the private
 * scanner mode):
 * no glasses connect gate, no camera stream, no detection boxes or
 * heatmap, no Meta AI vision classification, no HUD bitmap composition,
 * no ride debrief, no NAV routing, no fatigue / VOx computation. The
 * [SafetyRangeMonitor] accepts an optional VOx score, but this repo
 * computes none today, so VOx is skipped until an event-built engine
 * supplies one. Hazard warnings themselves live in hud/ (Hazard,
 * HazardAlerter) and factual voice readouts in voice/ (StatusReadout),
 * both written fresh for this repo.
 *
 * Stopping mirrors the private shell: leaving VirtuRide stops the ride
 * (GPS included) — see [stopIfRunning], called by MainActivity on
 * explicit STOP and on destroy.
 */
class VirtuRideMode(ctx: Context, ranges: SafetyRanges = SafetyRanges()) {

    val gps = GpsTracker(ctx)
    private val monitor = SafetyRangeMonitor(ranges)

    var running = false
        private set

    fun start() {
        if (running) return
        running = true
        gps.start()
    }

    fun stop() {
        if (!running) return
        running = false
        gps.stop()
    }

    /** No-op unless a ride is running — the private shell's leave-mode call. */
    fun stopIfRunning() {
        if (running) stop()
    }

    /** GPS speed / elevation / distance over the sensor feed's other values. */
    fun snapshot(base: RiderState): RiderState {
        if (!running) return base
        return base.copy(
            speedKmh = gps.speedKmh,
            elevationM = gps.altitudeM ?: base.elevationM,
            distanceKm = gps.distanceM / 1000.0
        )
    }

    fun rangeReport(state: RiderState, voxScore: Int? = null): String =
        monitor.report(state, voxScore)

    fun overLimitAlerts(state: RiderState, voxScore: Int? = null): List<String> =
        monitor.overLimitAlerts(state, voxScore)

    fun elapsedMs(): Long = gps.elapsedMs()
}
