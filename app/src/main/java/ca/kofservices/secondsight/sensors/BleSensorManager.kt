package ca.kofservices.secondsight.sensors

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

/**
 * BLE GATT skeleton for standard cycling / heart-rate sensors.
 * Parsing functions are complete; connection handling is stubbed with TODOs.
 */
class BleSensorManager(private val listener: Listener) {

    interface Listener {
        fun onRiderState(state: RiderState)
    }

    companion object {
        val HEART_RATE_SERVICE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val CSC_SERVICE: UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")
        val CSC_MEASUREMENT: UUID = UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb")

        /** Heart Rate Measurement (0x2A37): flags bit 0 selects UINT8 vs UINT16 bpm. */
        fun parseHeartRate(value: ByteArray): Int? {
            if (value.size < 2) return null
            val flags = value[0].toInt()
            return if (flags and 0x01 == 0) {
                value[1].toInt() and 0xFF
            } else {
                if (value.size < 3) null
                else (value[1].toInt() and 0xFF) or ((value[2].toInt() and 0xFF) shl 8)
            }
        }

        /**
         * CSC Measurement (0x2A5B): flags bit 0 = wheel data present
         * (UINT32 cumulative revolutions + UINT16 event time),
         * bit 1 = crank data present (UINT16 revolutions + UINT16 event time).
         * Returns (wheelRevolutions, crankRevolutions), either may be null.
         */
        fun parseCsc(value: ByteArray): Pair<Long?, Int?> {
            if (value.isEmpty()) return null to null
            val flags = value[0].toInt()
            var offset = 1
            var wheel: Long? = null
            var crank: Int? = null
            if (flags and 0x01 != 0 && value.size >= offset + 6) {
                wheel = (0..3).fold(0L) { acc, i -> acc or ((value[offset + i].toLong() and 0xFF) shl (8 * i)) }
                offset += 6
            }
            if (flags and 0x02 != 0 && value.size >= offset + 2) {
                crank = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            }
            return wheel to crank
        }
    }

    private var gatt: BluetoothGatt? = null
    private var latest = RiderState(0.0, null, null, 0.0, 0.0, 0.0)

    // TODO(hackathon): scan for devices exposing 0x180D / 0x1816, connect,
    // discover services, and enable notifications on the measurement
    // characteristics (runtime BLUETOOTH_SCAN / BLUETOOTH_CONNECT flow).
    fun connect() { /* stub — see TODO above */ }

    fun disconnect() {
        gatt?.close()
        gatt = null
    }

    /** Entry point a BluetoothGattCallback would call for each notification. */
    fun onCharacteristicBytes(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        when (characteristic.uuid) {
            HEART_RATE_MEASUREMENT -> parseHeartRate(value)?.let {
                latest = latest.copy(heartRateBpm = it)
                listener.onRiderState(latest)
            }
            CSC_MEASUREMENT -> {
                // TODO(hackathon): derive cadence/speed from revolution deltas
                // over event time and publish into `latest`.
                parseCsc(value)
            }
        }
    }
}
