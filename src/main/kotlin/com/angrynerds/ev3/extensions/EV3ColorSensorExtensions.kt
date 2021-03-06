package com.angrynerds.ev3.extensions

import lejos.hardware.sensor.EV3ColorSensor
import lejos.robotics.Color


fun EV3ColorSensor.awtColor(): java.awt.Color? {
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

fun EV3ColorSensor.htmlColor(): String {
    val color = this.awtColor()
    if (color != null) {
        return String.format("#%02x%02x%02x", color.red, color.green, color.blue)
    } else {
        return "#c44569"
    }
}