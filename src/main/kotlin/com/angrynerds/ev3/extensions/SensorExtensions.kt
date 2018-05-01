package com.angrynerds.ev3.extensions

import lejos.hardware.sensor.EV3ColorSensor
import lejos.hardware.sensor.EV3IRSensor
import lejos.hardware.sensor.EV3UltrasonicSensor
import lejos.robotics.Color

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

fun EV3ColorSensor.getColor(): java.awt.Color? {
    when (colorID) {
        Color.BLACK -> {
            return java.awt.Color.BLACK
        }
        Color.BLUE -> {
            return java.awt.Color.BLUE
        }
        Color.GREEN -> {
            return java.awt.Color.GREEN
        }
        Color.YELLOW -> {
            return java.awt.Color.YELLOW
        }
        Color.RED -> {
            return java.awt.Color.RED
        }
        Color.WHITE -> {
            return java.awt.Color.WHITE
        }
        Color.BROWN -> {
            return java.awt.Color(139, 69, 19)
        }
        else -> {
            return null
        }
    }
}

fun Color.getAwtColor(): java.awt.Color? {
    when (this.color) {
        Color.BLACK -> {
            return java.awt.Color.BLACK
        }
        Color.BLUE -> {
            return java.awt.Color.BLUE
        }
        Color.GREEN -> {
            return java.awt.Color.GREEN
        }
        Color.YELLOW -> {
            return java.awt.Color.YELLOW
        }
        Color.RED -> {
            return java.awt.Color.RED
        }
        Color.WHITE -> {
            return java.awt.Color.WHITE
        }
        Color.BROWN -> {
            return java.awt.Color(139, 69, 19)
        }
        else -> {
            return null
        }
    }
}

fun Color.getColorString(): String {
    when (this.color) {
        Color.BLACK -> {
            return "BLACK"
        }
        Color.BLUE -> {
            return "BLUE"
        }
        Color.GREEN -> {
            return "GREEN"
        }
        Color.YELLOW -> {
            return "YELLOW"
        }
        Color.RED -> {
            return "RED"
        }
        Color.WHITE -> {
            return "WHITE"
        }
        Color.BROWN -> {
            return "BROWN"
        }
        else -> {
            return "UNKNOWN COLOR"
        }
    }
}

fun EV3ColorSensor.getHtmlColor(): String {
    val color = this.getColor()
    if (color != null) {
        return String.format("#%02x%02x%02x", color.red, color.green, color.blue)
    } else {
        return "#c44569"
    }
}