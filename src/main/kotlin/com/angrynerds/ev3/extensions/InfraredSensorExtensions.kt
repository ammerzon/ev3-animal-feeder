package com.angrynerds.ev3.extensions

import lejos.hardware.sensor.EV3IRSensor

fun EV3IRSensor.measureDistance(): Float {
    val distanceSample = floatArrayOf(0f)
    distanceMode.fetchSample(distanceSample, 0)
    return distanceSample[0]
}
