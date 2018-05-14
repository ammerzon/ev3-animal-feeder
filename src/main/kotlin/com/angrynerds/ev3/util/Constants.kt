package com.angrynerds.ev3.util

import lejos.sensors.ColorId

object Constants {
    object Calibration {
        const val WAITING_TIME = 2
    }

    object PrecipiceDetection {
        // vertical distance between the floor and the ultrasonic sensor when the grabber position is up
        const val ULTRASONIC_THRESHOLD_GRABBER_UP = 0.2
        // vertical distance between the floor and the ultrasonic sensor when the grabber position is down
        const val ULTRASONIC_THRESHOLD_GRABBER_DOWN = 0.031000002
        const val ROTATION_ANGLE = 90.0
        const val BACKWARD_TRAVEL_DISTANCE = -100.0
    }

    object ObstacleCheck {
        // color of the tree trunk
        val TREE_COLOR = ColorId.BROWN
        // color of Winnie Pooh's feed color
        val WINNIE_POOH_FEED_COLOR = ColorId.YELLOW
        // color of I-AahÄs feed color
        val I_AAH_FEED_COLOR = ColorId.GREEN
        // colors of the other animals placed in the arena (e. g. tiger, ...)
        val ANIMAL_COLORS = arrayOf(ColorId.BLACK, ColorId.WHITE, ColorId.BLUE, ColorId.RED, ColorId.WHITE)
        // height of feed
        val FEED_HEIGHT = 1    // TODO: Configure
        // height of fence
        val FENCE_HEIGHT = 1.5      // TODO: Configure
        // distance of obstacles to be recognized
        val MIN_OBSTACLE_DISTANCE = 15
        // rotation angle of robot after an obstacle was detected
        val ROTATION_ANGLE = PrecipiceDetection.ROTATION_ANGLE + 30
    }

    object StableDetection {
        val ULTRASONIC_THRESHOLD = 10
        val WINNIE_POOH_STABLE_COLOR = ColorId.YELLOW // TODO: configure
        val I_AAH_FEED_COLOR = ColorId.RED // TODO: configure
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
        const val CLOSING_ANGLE = 2100 //TODO: configure
    }

    object Movement {
        const val DEFAULT_SPEED = 400.0
    }
}