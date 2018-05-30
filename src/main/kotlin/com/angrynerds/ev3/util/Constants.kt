package com.angrynerds.ev3.util

import lejos.sensors.ColorId

object Constants {
    object Calibration {
        const val WAITING_TIME = 2
    }

    object Reset {
        val ULTRASONIC_THRESHOLD_GRABBER_DOWN = 0.0..0.06
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
        // distance of obstacles to be recognized
        val MIN_OBSTACLE_DISTANCE = 5f
        // rotation angle of robot after an obstacle was detected
        val ROTATION_ANGLE = PrecipiceDetection.ROTATION_ANGLE + 30
        // height at which is a robot detected above
        const val ROBOT_DETECTION_MIN_HEIGHT = 8f
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

    object Grabber {
        const val UPWARD_SPEED = 400
        const val DOWNWARD_SPEED = 200
        // rotations needed to raise the grabber
        const val TOTAL_ANGLE = 5400
        const val CLOSING_ANGLE = 2100
    }

    object Movement {
        const val DEFAULT_SPEED = 80.0
    }

    object Sensors {
        const val IRCmPerValue = 0.4f
        const val USCmPerValue = 120.0f
    }
}