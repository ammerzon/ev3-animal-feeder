package com.angrynerds.ev3.enums

import com.angrynerds.ev3.util.Constants

enum class GripperArmPosition(val rotations: Double, val height: Float) {
    TOP(Constants.GripperArm.TOP_ANGLE, 21f),
    BOTTOM_CLOSED(Constants.GripperArm.BOTTOM_CLOSED_ANGLE, 5.5f),
    BOTTOM_OPEN(Constants.GripperArm.BOTTOM_OPEN_ANGLE, 5.5f),
    MIDDLE(Constants.GripperArm.MIDDLE_ANGLE, 13f);
    
    fun isBottom(): Boolean {
        return this == BOTTOM_CLOSED || this == BOTTOM_OPEN
    }
}