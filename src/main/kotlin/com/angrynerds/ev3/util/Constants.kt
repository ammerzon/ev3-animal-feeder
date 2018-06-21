package com.angrynerds.ev3.util

import com.angrynerds.ev3.lejos.robotics.ColorId

object Constants {
    object FeedScan {
        const val SCAN_TIMES = 1
    }

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
        const val ROTATION_ANGLE = 60.0
        const val BACKWARD_TRAVEL_DISTANCE = -120.0
    }

    object ObstacleCheck {
        // color of the tree trunk
        val TREE_COLOR = ColorId.BROWN
        // color of Winnie Pooh's feed color
        val WINNIE_POOH_FEED_COLOR = ColorId.YELLOW
        // color of I-AahÄs feed color
        val I_AAH_FEED_COLOR = ColorId.GREEN
        // colors of the other animals placed in the arena (e. g. tiger, ...)
        val NOT_ANIMAL_COLORS = arrayOf(ColorId.BROWN, ColorId.GREEN, ColorId.YELLOW, ColorId.WHITE, ColorId.BLUE, ColorId.NONE, ColorId.BLACK) // TODO check if up to date
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
        val STABLE_HEIGHT = 4f
        val WINNIE_POOH_STABLE_COLOR = ColorId.BLUE
        val I_AAH_STABLE_COLOR = ColorId.WHITE
    }

    object GripperArm {
        const val SPEED = 800
        const val BOTTOM_OPEN_ANGLE = 0.0
        const val BOTTOM_CLOSED_ANGLE = 1700.0
        const val MIDDLE_ANGLE = BOTTOM_CLOSED_ANGLE + 1500.0
        const val STABLE_ANGLE = BOTTOM_CLOSED_ANGLE + 1200.0
        const val TOP_ANGLE = BOTTOM_CLOSED_ANGLE + 3300.0
    }

    object Movement {
        const val DEFAULT_SPEED = 80.0
        const val SLOW_SPEED = 25.0
        const val HIGH_SPEED = 160.0
    }

    object Sensors {
        const val IRCmPerValue = 0.4f // infrared cm per value
        const val USCmPerValue = 120.0f // ultrasonic cm per value
    }
}