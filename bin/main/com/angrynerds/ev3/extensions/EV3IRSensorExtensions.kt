package com.angrynerds.ev3.extensions

import com.angrynerds.ev3.util.Constants
import lejos.hardware.sensor.EV3IRSensor

fun EV3IRSensor.getDistance(): Float {
    val distanceSample = floatArrayOf(0f)
    distanceMode.fetchSample(distanceSample, 0)
    return distanceSample[0]
}

fun getCmFromIRValue(value: Float): Float {
    if (value.isInfinite())
        return Float.POSITIVE_INFINITY
    return value * Constants.Sensors.IRCmPerValue
}