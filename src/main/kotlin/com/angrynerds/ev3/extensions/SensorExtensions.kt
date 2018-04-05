package com.angrynerds.ev3.extensions

import lejos.hardware.sensor.EV3ColorSensor
import lejos.hardware.sensor.EV3IRSensor
import lejos.hardware.sensor.EV3UltrasonicSensor
import java.awt.Color

fun EV3IRSensor.getDistance(): Float {
    val distanceSample = floatArrayOf(0f)
    distanceMode.fetchSample(distanceSample, 0)
    return distanceSample[0]
}

fun EV3UltrasonicSensor.getDistance(): Float {
    val distanceSample = floatArrayOf(0f)
    distanceMode.fetchSample(distanceSample, 0)
    return distanceSample[0]
}

fun EV3ColorSensor.getColor(): Color {
    val rgbMode = this.rgbMode
    val sample = FloatArray(rgbMode.sampleSize())
    rgbMode.fetchSample(sample, 0)
    val r = sample[0]
    val g = sample[1]
    val b = sample[2]

    return Color(r, g, b);
}

fun EV3ColorSensor.getHtmlColor(): String {
    val color = this.getColor();
    return String.format("#%02x%02x%02x", color.red, color.green, color.blue)
}

fun EV3ColorSensor.getColorIDFromSample(): Float {
    val rgbMode = this.colorIDMode
    val sample = FloatArray(rgbMode.sampleSize())
    rgbMode.fetchSample(sample, 0)
    return sample[0];
}