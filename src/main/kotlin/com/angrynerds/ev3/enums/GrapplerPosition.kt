package com.angrynerds.ev3.enums

import com.angrynerds.ev3.util.Constants


enum class GrapplerPosition(val rotations: Double) {
    TOP(Constants.Grabber.TOTAL_ANGLE.toDouble()),
    BOTTOM_CLOSED(Constants.Grabber.CLOSING_ANGLE.toDouble()),
    BOTTOM_OPEN(0.0),
    MIDDLE(Constants.Grabber.CLOSING_ANGLE + 400.0);

    val state = if (rotations == 0.0) GrapplerState.OPEN else GrapplerState.CLOSED

    fun isBottom(): Boolean {
        return this == BOTTOM_CLOSED || this == BOTTOM_OPEN
    }
}