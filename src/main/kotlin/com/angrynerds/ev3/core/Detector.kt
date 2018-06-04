package com.angrynerds.ev3.core

import com.angrynerds.ev3.debug.EV3LogHandler
import com.angrynerds.ev3.enums.DetectionMode
import com.angrynerds.ev3.enums.DetectionType
import com.angrynerds.ev3.enums.GripperArmPosition
import com.angrynerds.ev3.enums.Obstacle
import com.angrynerds.ev3.extensions.getCmFromIRValue
import com.angrynerds.ev3.extensions.getCmFromUSValue
import com.angrynerds.ev3.util.Constants
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import lejos.sensors.ColorId
import java.util.logging.LogManager
import java.util.logging.Logger

object Detector {
    private var logger = Logger.getLogger("detector")
    val detectionSubject: PublishSubject<DetectionType> = PublishSubject.create()
    private val subscribers = CompositeDisposable()

    val obstacles: Observable<ObstacleInfo> = detectionSubject.filter { it == DetectionType.OBSTACLE }
            .filter { currentObstacleInfo != null }.map { currentObstacleInfo }

    fun obstacles(obstacleType: Obstacle): Observable<ObstacleInfo> {
        return obstacles.filter {
            it.isObstacle(obstacleType, true)
        }
    }

    var currentObstacleInfo: ObstacleInfo? = null
    var detectionMode = DetectionMode.DEFAULT

    fun start() {
        logger = LogManager.getLogManager().getLogger("detector")
        logger.addHandler(EV3LogHandler())
        logger.info("Start detecting")
        val ultrasonicObservable = RxFeederRobot.rxUltrasonicSensor.distance
        val infraredObservable = RxFeederRobot.rxInfraredSensor.distance
        val colorForwardObservable = RxFeederRobot.rxColorSensorForward.colorId
        val colorVerticalObservable = RxFeederRobot.rxColorSensorVertical.colorId

        subscribers.addAll(
                ultrasonicObservable.subscribe(::onUltrasonicSensor),
                infraredObservable.subscribe(::onInfraredSensor),
                colorForwardObservable.subscribe(::onForwardColorSensor),
                colorVerticalObservable.subscribe(::onVerticalColorSensor)
        )
    }

    private fun startObstacleDetection(): ObstacleInfo {
        val obstacleInfo = ObstacleInfo()
        currentObstacleInfo = obstacleInfo
        return obstacleInfo
    }

    private fun endObstacleDetection() {
        currentObstacleInfo = null
    }

    private fun ensureObstacleDetection(): ObstacleInfo {
        return currentObstacleInfo ?: startObstacleDetection()
    }

    private fun onHeight(height: Float) {
        logger.info("height: " + height)
        if (height < -10) {
            onPrecipice()
            return
        }

        if (detectionMode == DetectionMode.SEARCH_OBSTACLE_COLOR)
            return

        val obstacleInfo = ensureObstacleDetection()
        obstacleInfo.onSensorDetectedHeight(height)

        if (!obstacleInfo.anyObstaclePossible()) {
            // no obstacle (table height detected)
            endObstacleDetection()
            emitDetection(DetectionType.NOTHING)
            return
        }

        if (obstacleInfo.isObstacle(Obstacle.ROBOT, true))
            emitDetection(DetectionType.ROBOT)
        else
            emitDetection(DetectionType.OBSTACLE)
    }

    private fun onDistance(distance: Float) {
        if (distance <= Constants.ObstacleCheck.ROBOT_DETECTION_MAX_DISTANCE)
            emitDetection(DetectionType.ROBOT)
    }

    private fun onPrecipice() {
        emitDetection(DetectionType.PRECIPICE, true)
    }

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
        onForwardColor(color)
    }

    private fun onVerticalColorSensor(color: ColorId) {
        onVerticalColor(color)
    }

    private fun onForwardColor(colorId: ColorId) {
        if (detectionMode == DetectionMode.SEARCH_OBSTACLE_HEIGHT)
            return

        if (FeederRobot.gripperArmPosition != GripperArmPosition.BOTTOM_OPEN)
            return

        val obstacleInfo = ensureObstacleDetection()
        obstacleInfo.onSensorDetectedColorForward(colorId)

        if (!obstacleInfo.anyObstaclePossible()) {
            // no obstacle (table height detected)
            endObstacleDetection()
            emitDetection(DetectionType.NOTHING)
            return
        }

        emitDetection(DetectionType.OBSTACLE)
    }

    private fun onVerticalColor(colorId: ColorId) {
        if (detectionMode == DetectionMode.SEARCH_OBSTACLE_HEIGHT)
            return

        if (FeederRobot.gripperArmPosition.isBottom())
            return

        val obstacleInfo = ensureObstacleDetection()
        obstacleInfo.onSensorDetectedColorVertical(colorId)

        if (!obstacleInfo.anyObstaclePossible()) {
            // no obstacle (table height detected)
            endObstacleDetection()
            emitDetection(DetectionType.NOTHING)
            return
        }

        emitDetection(DetectionType.OBSTACLE)
    }

    private fun emitDetection(detectionType: DetectionType, force: Boolean = false) {
        if (!force && detectionMode == DetectionMode.IGNORE)
            return
        detectionSubject.onNext(detectionType)
    }
}