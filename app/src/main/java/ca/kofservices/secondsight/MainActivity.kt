package ca.kofservices.secondsight

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import ca.kofservices.secondsight.hud.HudRenderer
import ca.kofservices.secondsight.hud.HudState
import ca.kofservices.secondsight.sensors.MockSensorFeed
import ca.kofservices.secondsight.sensors.RiderState
import ca.kofservices.secondsight.virturide.VirtuRideMode
import ca.kofservices.secondsight.voice.StatusReadout

/**
 * Day-1 scaffold activity: phone-side stand-in for the glasses HUD.
 *
 * VirtuRide mode wiring (ported Nov 2, 2026 from the private build's
 * VIRTURIDE tab, minus everything that stays private): START RIDE /
 * STOP RIDE drive [VirtuRideMode] — phone GPS speed, elevation and
 * distance plus Safety Ranges evaluation — rendered through the same
 * [HudRenderer] / [StatusReadout] path as the mock feed. When no ride
 * is running, "Next reading" behaves exactly as before.
 *
 * Differences from the private shell, by design: no glasses-connect
 * gate (this repo has no DAT session yet — see the TODO in
 * app/build.gradle.kts), no private-scanner tab, no detection stream. START
 * RIDE only gates on the location permission, using the private
 * build's request code (9201) and prompt wording.
 */
class MainActivity : Activity(), HudRenderer {

    private val feed = MockSensorFeed()
    private lateinit var readoutView: TextView
    private lateinit var rideStatusView: TextView
    private lateinit var rideButton: Button
    private var latestState: HudState? = null

    private val virturide by lazy { VirtuRideMode(this) }

    companion object {
        /** Same location request code as the private VirtuRide build. */
        private const val REQ_LOCATION = 9201
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readoutView = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
            text = "Tap Next reading to step the mock sensor feed."
        }
        rideStatusView = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            text = "VirtuRide ride mode — GPS + Safety Ranges. Tap START RIDE."
        }
        val nextButton = Button(this).apply {
            text = "Next reading"
            setOnClickListener { advance() }
        }
        rideButton = Button(this).apply {
            text = "▶ START RIDE"
            setOnClickListener { toggleRide() }
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            addView(readoutView)
            addView(nextButton)
            addView(rideStatusView)
            addView(rideButton)
        }
        setContentView(layout)
    }

    override fun onDestroy() {
        // Leaving VirtuRide stops the ride (GPS included), as in the private shell.
        virturide.stopIfRunning()
        super.onDestroy()
    }

    private fun toggleRide() {
        if (virturide.running) {
            virturide.stop()
            rideButton.text = "▶ START RIDE"
            rideStatusView.text = "Ride stopped. Tap START RIDE to go again."
            return
        }
        if (!virturide.gps.hasPermission()) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_LOCATION)
            Toast.makeText(this, "Grant location permission, then tap START RIDE again", Toast.LENGTH_SHORT).show()
            return
        }
        virturide.start()
        rideButton.text = "■ STOP RIDE"
        rideStatusView.text = "Ride started — GPS speed live. Tap Next reading to refresh."
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            virturide.start()
            rideButton.text = "■ STOP RIDE"
            rideStatusView.text = "Ride started — GPS speed live. Tap Next reading to refresh."
        }
    }

    private fun advance() {
        val base: RiderState = feed.next()
        val rider: RiderState = if (virturide.running) virturide.snapshot(base) else base
        render(HudState(rider = rider, hazards = emptyList(), heroMetric = "Speed"))
    }

    override fun render(state: HudState) {
        latestState = state
        var text = StatusReadout.format(state.rider, state.hazards)
        if (virturide.running) {
            text += "\n" + virturide.rangeReport(state.rider)
            val alerts = virturide.overLimitAlerts(state.rider)
            if (alerts.isNotEmpty()) {
                text += "\n" + alerts.joinToString(" ")
                rideStatusView.text = alerts.joinToString(" ")
            }
        }
        readoutView.text = text
    }
}
