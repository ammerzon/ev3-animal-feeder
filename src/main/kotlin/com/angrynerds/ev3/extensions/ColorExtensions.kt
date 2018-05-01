package com.angrynerds.ev3.extensions

import lejos.robotics.Color

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