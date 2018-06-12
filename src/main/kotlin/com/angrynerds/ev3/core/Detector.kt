package com.angrynerds.ev3.core

import com.angrynerds.ev3.debug.EV3LogHandler
import com.angrynerds.ev3.enums.GripperArmPosition
import com.angrynerds.ev3.enums.Obstacle
import com.angrynerds.ev3.extensions.getCmFromIRValue
import com.angrynerds.ev3.extensions.getCmFromUSValue
import com.angrynerds.ev3.extensions.isValidFeedColor
import com.angrynerds.ev3.util.Constants
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import lejos.sensors.ColorId
import java.util.logging.LogManager
import java.util.logging.Logger

object Detector {
    private var logger = Logger.getLogger("detector")

    val obstacles: PublishSubject<Obstacle> = PublishSubject.create()
    private val subscribers = CompositeDisposable()
    var detectionType = DetectionType.FEED_SCAN

    var scannedColorId = ColorId.NONE
    private var feedScanned = false

    init {
        logger = LogManager.getLogManager().getLogger("detector")
        logger.addHandler(EV3LogHandler())
        logger.info("Detector started")
    }

    fun startScan() {
        logger.info("Start scanning feed")

        detectionType = DetectionType.FEED_SCAN
        feedScanned = false
        scannedColorId = ColorId.NONE

        onForwardColorSensor(ColorId.Companion.colorId(FeederRobot.colorSensorForward.colorID))

        subscribers.clear()
    }

    fun startDetectingObstacles() {
        detectionType = DetectionType.OBSTACLE
        logger.info("Start detecting obstacles")

        val ultrasonicObservable = RxFeederRobot.rxUltrasonicSensor.distance
        val infraredObservable = RxFeederRobot.rxInfraredSensor.distance
        val colorVerticalObservable = RxFeederRobot.rxColorSensorVertical.colorId
        val colorForwardObservable = RxFeederRobot.rxColorSensorForward.colorId

        subscribers.addAll(
                ultrasonicObservable.subscribe(::onUltrasonicSensor),
                infraredObservable.subscribe(::onInfraredSensor),
                colorVerticalObservable.subscribe(::onVerticalColorSensor),
                colorForwardObservable.subscribe(::onForwardColorSensor)
        )

        if (!feedScanned) {
            logger.warning("Detecting obstacles called without feed scanned.")
        }
    }


    // region subscribers
    private fun onUltrasonicSensor(distance: Float) {
        if (distance.isFinite()) {
            val distanceInCm = getCmFromUSValue(distance)
            val height = FeederRobot.gripperArmPosition.height - distanceInCm
            onHeight(height)
        }
    }

    private fun onInfraredSensor(distance: Float) {
        val distanceInCm = getCmFromIRValue(distance)
        onDistance(distanceInCm)
    }

    private fun onForwardColorSensor(color: ColorId) {
        if (detectionType == DetectionType.OBSTACLE) {
            onObstacleForwardColor(color)
        } else if (detectionType == DetectionType.FEED_SCAN && !feedScanned) {
            onFeedScanForwardColor(color)
        }
    }

    private fun onVerticalColorSensor(color: ColorId) {
        onVerticalColor(color)
    }
    // endregion

    // region helpers
    private fun detect(obstacle: Obstacle) {
        obstacles.onNext(obstacle)
    }
    // endregion

    // region events
    private fun onHeight(height: Float) {
        logger.info("height: $height")
        when {
            height < -10 -> {
                detect(Obstacle.PRECIPICE)
                return
            }
            isStableHeight(height) -> {
                detect(Obstacle.STABLE_HEIGHT)
            }
//            isFenceHeight(height) -> {
//                detect(Obstacle.FENCE)
//            }
        }
    }

    private fun isFenceHeight(height: Float): Boolean {
        // TODO check if measured height is fence height - consider the height of the gripper arm!
        return false
    }

    private fun isStableHeight(height: Float): Boolean {
        logger.info("isStableHeight? $height")
        return height >= 6
    }

    private fun onDistance(distance: Float) {
        logger.info("distance: $distance")
        if (distance <= Constants.ObstacleCheck.ROBOT_DETECTION_MAX_DISTANCE) {
            detect(Obstacle.ROBOT)
        }
    }

    private fun onObstacleForwardColor(colorId: ColorId) {
        logger.info("forward-color: $colorId")
        if (colorId == Constants.ObstacleCheck.TREE_COLOR) {
//            detect(Obstacle.TREE)
        } else if (!Constants.ObstacleCheck.NOT_ANIMAL_COLORS.contains(colorId)) {
            detect(Obstacle.ANIMAL)
        } else if (FeederRobot.gripperArmPosition == GripperArmPosition.BOTTOM_OPEN) {
            if (colorId == FeederRobot.feedColor) {
                detect(Obstacle.FEED)
            } else if (colorId == FeederRobot.opponentFeedColor) {
                detect(Obstacle.FEED_OPPONENT)
            }
        }
    }

    private fun onVerticalColor(colorId: ColorId) { // only for stable detection
        logger.info("vertical-color: $colorId")
        if (colorId == FeederRobot.stableColor) {
            detect(Obstacle.STABLE)
        } else if (colorId == FeederRobot.opponentStableColor) {
            detect(Obstacle.STABLE_OPPONENT)
        }
    }

    private fun onFeedScanForwardColor(colorId: ColorId) {
        logger.info("forward-color (Mode.SCAN): $colorId")

        if (colorId.isValidFeedColor()) {
            logger.info("$colorId is the feed color")
            this.scannedColorId = colorId
            FeederRobot.feedColor = scannedColorId
            this.feedScanned = true
        } else {
            logger.info("Invalid feed color detected: $colorId")
            logger.info("Retrying ...")
            onForwardColorSensor(ColorId.Companion.colorId(FeederRobot.colorSensorForward.colorID))
        }
    }
    // endregion

    enum class DetectionType {
        OBSTACLE, FEED_SCAN
    }
}