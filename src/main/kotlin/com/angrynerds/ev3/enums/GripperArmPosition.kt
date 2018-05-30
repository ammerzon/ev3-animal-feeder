package com.angrynerds.ev3.enums

import com.angrynerds.ev3.util.Constants


enum class GripperArmPosition(val rotations: Double) {
    BOTTOM_CLOSED(Constants.GripperArm.BOTTOM_CLOSED_ANGLE),
    BOTTOM_OPEN(0.0),
    MIDDLE(Constants.GripperArm.MIDDLE_ANGLE),
    TOP(Constants.GripperArm.TOP_ANGLE);

    /**
     * Checks if the position is [GripperArmPosition.BOTTOM_CLOSED] or [GripperArmPosition.BOTTOM_OPEN].
     *
     * @return `True` if the position is [GripperArmPosition.BOTTOM_CLOSED] or [GripperArmPosition.BOTTOM_OPEN]; `False` otherwise.
     */
    fun isInBottomPosition(): Boolean {
        return this == BOTTOM_CLOSED || this == BOTTOM_OPEN
    }
}