package ca.kofservices.secondsight.virturide

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

/**
 * Phone GPS source for a VirtuRide ride: ground speed, position, altitude
 * and accumulated distance, from the GPS provider only.
 *
 * Ported (Nov 2, 2026) from the VirtuRide GPS logic in the private
 * kof-glass-app repo (feature/v17.11, VirtuRideScreens.GpsTracker).
 * This copy is self-contained: it needs only a [Context] and the
 * ACCESS_FINE_LOCATION permission declared in AndroidManifest.xml. It
 * does NOT depend on the private glasses session, ViewModel, or the
 * private scanner mode — none of those cross into this repo.
 *
 * Behaviour kept from the source: 1 s / 1 m updates, speed from the fix
 * when present (otherwise distance-over-time between fixes), distance
 * accumulated only for 1–200 m steps between fixes, and last-known
 * location seeded at start. All state is volatile because fixes arrive
 * on the location thread while the UI reads them.
 */
class GpsTracker(private val ctx: Context) {

    @Volatile var speedKmh = 0.0
    @Volatile var lat: Double? = null
    @Volatile var lon: Double? = null
    @Volatile var altitudeM: Double? = null
    @Volatile var distanceM = 0.0
    @Volatile private var startTimeMs = 0L
    @Volatile private var lastLoc: Location? = null

    private val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val listener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            lat = loc.latitude
            lon = loc.longitude
            if (loc.hasSpeed() && loc.speed >= 0) {
                speedKmh = loc.speed * 3.6
            } else {
                lastLoc?.let { prev ->
                    val dt = (loc.time - prev.time) / 1000.0
                    if (dt > 0.5) speedKmh = (loc.distanceTo(prev) / dt) * 3.6
                }
            }
            if (loc.hasAltitude()) altitudeM = loc.altitude
            lastLoc?.let { prev ->
                val d = loc.distanceTo(prev)
                if (d in 1.0..200.0) distanceM += d
            }
            lastLoc = loc
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}

        @Deprecated("Deprecated in API 29")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    fun hasPermission(): Boolean =
        ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun start() {
        distanceM = 0.0
        speedKmh = 0.0
        lat = null
        lon = null
        altitudeM = null
        lastLoc = null
        startTimeMs = System.currentTimeMillis()
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, listener)
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { listener.onLocationChanged(it) }
        } catch (_: SecurityException) {
        } catch (_: Throwable) {
        }
    }

    fun stop() {
        try {
            lm.removeUpdates(listener)
        } catch (_: Throwable) {
        }
    }

    fun elapsedMs(): Long =
        if (startTimeMs == 0L) 0 else System.currentTimeMillis() - startTimeMs
}
