package com.angrynerds.ev3.core

import com.angrynerds.ev3.enums.Obstacle
import com.angrynerds.ev3.extensions.getCmFromIRValue
import com.angrynerds.ev3.extensions.getCmFromUSValue
import com.angrynerds.ev3.logger
import com.angrynerds.ev3.util.Constants
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import lejos.sensors.ColorId
import java.util.concurrent.TimeUnit


object Detector {
    private val detectionSubject: Subject<DetectionType> = PublishSubject.create()
    private val subscribers = CompositeDisposable()

    val detections = detectionSubject
            .doOnSubscribe { start() }
            .doOnDispose{ subscribers.clear() }!!

    val obstacles = detections.filter { it == DetectionType.OBSTACLE }.map { currentObstacleInfo!! }!!

    fun obstacles(obstacleType: Obstacle): Observable<ObstacleInfo> {
        return obstacles.filter {
            it.isObstacle(obstacleType, true)
        }
    }

    var currentObstacleInfo: ObstacleInfo? = null
    var detectionMode = DetectionMode.DEFAULT

    private fun start() {
        logger.info("start detecting")
        val timeout = 500L //ms
        val ultrasonicObservable = RxFeederRobot.rxUltrasonicSensor.distance
                .timeout(timeout, TimeUnit.MILLISECONDS)
        val infraredObservable = RxFeederRobot.rxInfraredSensor.distance
                .timeout(timeout, TimeUnit.MILLISECONDS)
        val colorForwardObservable = RxFeederRobot.rxColorSensorForward.colorId
                .timeout(timeout, TimeUnit.MILLISECONDS)
        val colorVerticalObservable = RxFeederRobot.rxColorSensorVertical.colorId
                .timeout(timeout, TimeUnit.MILLISECONDS)

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
        return currentObstacleInfo?: startObstacleDetection()
    }

    private fun onHeight(height: Float) {
        if(height < -10) {
            onPrecipice()
            return
        }

        if(detectionMode == DetectionMode.SEARCH_OBSTACLE_COLOR)
            return

        val obstacleInfo = ensureObstacleDetection()
        obstacleInfo.onSensorDetectedHeight(height)

        if(!obstacleInfo.anyObstaclePossible()) {
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

    private fun onColor(colorId: ColorId) {
        if(detectionMode == DetectionMode.SEARCH_OBSTACLE_HEIGHT)
            return

        val obstacleInfo = ensureObstacleDetection()
        obstacleInfo.onSensorDetectedColor(colorId)

        if(!obstacleInfo.anyObstaclePossible()) {
            // no obstacle (table height detected)
            endObstacleDetection()
            emitDetection(DetectionType.NOTHING)
            return
        }

        emitDetection(DetectionType.OBSTACLE)
    }

    private fun onDistance(distance: Float) {
        if(distance <= Constants.ObstacleCheck.ROBOT_DETECTION_MAX_DISTANCE)
            emitDetection(DetectionType.ROBOT)
    }

    private fun onPrecipice() {
        emitDetection(DetectionType.PRECIPICE, true)
    }

    private fun onUltrasonicSensor(distance: Float) {
        logger.info("us value received")
        val distanceInCm = getCmFromUSValue(distance)
        val height = FeederRobot.gripperArmPosition.height - distanceInCm
        onHeight(height)
    }

    private fun onInfraredSensor(distance: Float) {
        val distanceInCm = getCmFromIRValue(distance)
        onDistance(distanceInCm)
    }

    private fun onForwardColorSensor(color: ColorId) {
        onColor(color)
    }

    private fun onVerticalColorSensor(color: ColorId) {
        onColor(color)
    }

    private fun emitDetection(detectionType: DetectionType, force: Boolean = false) {
        if (!force || detectionMode == DetectionMode.IGNORE)
            return
        detectionSubject.onNext(detectionType)
    }

    enum class DetectionMode {
        DEFAULT, SEARCH_OBSTACLE_HEIGHT, SEARCH_OBSTACLE_COLOR, IGNORE
    }

    enum class DetectionType {
        PRECIPICE, OBSTACLE, NOTHING, ROBOT
    }
}