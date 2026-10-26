package ca.kofservices.secondsight

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import ca.kofservices.secondsight.hud.HudRenderer
import ca.kofservices.secondsight.hud.HudState
import ca.kofservices.secondsight.sensors.MockSensorFeed
import ca.kofservices.secondsight.sensors.RiderState
import ca.kofservices.secondsight.voice.StatusReadout

/** Day-1 scaffold activity: phone-side stand-in for the glasses HUD. */
class MainActivity : Activity(), HudRenderer {

    private val feed = MockSensorFeed()
    private lateinit var readoutView: TextView
    private var latestState: HudState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readoutView = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
            text = "Tap Next reading to step the mock sensor feed."
        }
        val nextButton = Button(this).apply {
            text = "Next reading"
            setOnClickListener { advance() }
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            addView(readoutView)
            addView(nextButton)
        }
        setContentView(layout)
    }

    private fun advance() {
        val rider: RiderState = feed.next()
        render(HudState(rider = rider, hazards = emptyList(), heroMetric = "Speed"))
    }

    override fun render(state: HudState) {
        latestState = state
        readoutView.text = StatusReadout.format(state.rider, state.hazards)
    }
}
