package com.angrynerds.ev3.enums

import com.angrynerds.ev3.util.Constants

enum class GripperArmPosition(val rotations: Double, val height: Float) {
    TOP(Constants.GripperArm.TOP_ANGLE, 22.4f),
    BOTTOM_CLOSED(Constants.GripperArm.BOTTOM_CLOSED_ANGLE, 6.9f),
    BOTTOM_OPEN(Constants.GripperArm.BOTTOM_OPEN_ANGLE, 6.9f),
    MIDDLE(Constants.GripperArm.MIDDLE_ANGLE, 14.4f),
    STABLE(Constants.GripperArm.STABLE_ANGLE, 13.0f);
    
    fun isBottom(): Boolean {
        return this == BOTTOM_CLOSED || this == BOTTOM_OPEN
    }
}