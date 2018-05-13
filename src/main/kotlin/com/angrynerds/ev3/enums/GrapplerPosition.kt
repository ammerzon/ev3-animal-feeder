package com.angrynerds.ev3.enums

import com.angrynerds.ev3.util.Constants


enum class GrapplerPosition(val rotations: Double) {
    TOP(Constants.Grabber.GRAB_MOTOR_MAX_ROTATIONS.toDouble()),
    BOTTOM_CLOSED(Constants.Grabber.CLOSING_ROTATION.toDouble()),
    BOTTOM_OPEN(0.0),
    MIDDLE(Constants.Grabber.CLOSING_ROTATION + 200.0);

    val state = if (rotations == 0.0) GrapplerState.OPEN else GrapplerState.CLOSED

}