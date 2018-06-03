package com.angrynerds.ev3.util

import lejos.sensors.ColorId

object Constants {
    object Reset {
        val ULTRASONIC_GRABBER_DOWN = 0.0..0.06
        const val SPEED = 700
    }

    object PrecipiceDetection {
        // vertical distance between the floor and the ultrasonic sensor when the grabber position is up
        val ULTRASONIC_THRESHOLD_GRABBER_UP = 0.19..0.21
        // vertical distance between the floor and the ultrasonic sensor when the grabber position is down
        val ULTRASONIC_THRESHOLD_GRABBER_DOWN = 0.04..0.06
        // should be 90 degress
        const val ROTATION_ANGLE = 70.0
        const val BACKWARD_TRAVEL_DISTANCE = -150.0
    }

    object ObstacleCheck {
        // color of the tree trunk
        val TREE_COLOR = ColorId.BROWN
        // color of Winnie Pooh's feed color
        val WINNIE_POOH_FEED_COLOR = ColorId.YELLOW
        // color of I-AahÄs feed color
        val I_AAH_FEED_COLOR = ColorId.GREEN
        // colors of the other animals placed in the arena (e. g. tiger, ...)
        val NOT_ANIMAL_COLORS = arrayOf(ColorId.BROWN, ColorId.GREEN, ColorId.YELLOW, ColorId.WHITE, ColorId.BLUE)
        // height of feed
        val FEED_HEIGHT = 2f
        // height of fence 0.03..0.04
        val FENCE_HEIGHT = 3f
        val TREE_HEIGHT = 6f..20f
        val ANIMAL_HEIGHT = 4f..10f
        // distance of obstacles to be recognized
        val MIN_OBSTACLE_DISTANCE = 5f
        // rotation angle of robot after an obstacle was detected
        val ROTATION_ANGLE = PrecipiceDetection.ROTATION_ANGLE + 30
        // height at which is a robot detected above
        const val ROBOT_DETECTION_MIN_HEIGHT = 8f
        const val ROBOT_DETECTION_MAX_DISTANCE = 5f
    }

    object StableDetection {
        val STABLE_HEIGHT = 8f
        val WINNIE_POOH_STABLE_COLOR = ColorId.BLUE
        val I_AAH_FEED_COLOR = ColorId.WHITE
    }

    object DemolishFence {
        // vertical distance between the fence and the ultrasonic sensor
        val ULTRASONIC_THRESHOLD = 10
    }

    object GripperArm {
        const val SPEED = 800
        const val TOP_ANGLE = 5400.0
        const val BOTTOM_CLOSED_ANGLE = 2100.0
        const val BOTTOM_OPEN_ANGLE = 0.0
        const val MIDDLE_ANGLE = 2400.0
    }

    object Movement {
        const val DEFAULT_SPEED = 80.0
        const val SLOW_SPEED = 40.0 // TODO adjust value
        const val HIGH_SPEED = 160.0 // TODO adjust value
    }

    object Sensors {
        const val IRCmPerValue = 0.4f // infrared cm per value
        const val USCmPerValue = 120.0f // ultrasonic cm per value
    }
}