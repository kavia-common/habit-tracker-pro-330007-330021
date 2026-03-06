package org.example.app.util

// PUBLIC_INTERFACE
fun Double.toPercentInt(): Int {
    /** Converts a [0.0, 1.0] rate into [0, 100] percent int with clamping. */
    val v = (this * 100.0).toInt()
    return when {
        v < 0 -> 0
        v > 100 -> 100
        else -> v
    }
}
