package com.angrynerds.ev3.core

import com.angrynerds.ev3.extensions.getCmFromIRValue
import com.angrynerds.ev3.extensions.getCmFromUSValue
import com.angrynerds.ev3.util.Constants
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import lejos.sensors.ColorId
import java.util.concurrent.TimeUnit


object Detector {
    var detections: Subject<DetectionType> = PublishSubject.create()

    var currentObstacleInfo: ObstacleInfo? = null
    var detectionMode = DetectionMode.DEFAULT

    private val subscribers = CompositeDisposable()

    init {
        detections.doOnSubscribe { start() }
        detections.doOnDispose { subscribers.clear() }
    }

    private fun start() {
        val ultrasonicObservable = RxFeederRobot.rxUltrasonicSensor.distance
                .timeout(200, TimeUnit.MILLISECONDS)
        val infraredObservable = RxFeederRobot.rxInfraredSensor.distance
                .timeout(200, TimeUnit.MILLISECONDS)
        val colorForwardObservable = RxFeederRobot.rxColorSensorForward.colorId
                .timeout(200, TimeUnit.MILLISECONDS)
        val colorVerticalObservable = RxFeederRobot.rxColorSensorVertical.colorId
                .timeout(200, TimeUnit.MILLISECONDS)

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
        obstacleInfo.detectedHeight(height)

        if(!obstacleInfo.anyObstaclePossible()) {
            // no obstacle (table height detected)
            endObstacleDetection()
            emitDetection(DetectionType.NOTHING)
            return
        }

        if(obstacleInfo.isRobot())
            emitDetection(DetectionType.ROBOT)
        else
            emitDetection(DetectionType.OBSTACLE)
    }

    private fun onColor(colorId: ColorId) {
        if(detectionMode == DetectionMode.SEARCH_OBSTACLE_HEIGHT)
            return
    }

    private fun onDistance(distance: Float) {
        if(distance <= Constants.ObstacleCheck.ROBOT_DETECTION_MAX_DISTANCE)
            emitDetection(DetectionType.ROBOT)
    }

    private fun onPrecipice() {
        emitDetection(DetectionType.PRECIPICE, true)
    }

    private fun onUltrasonicSensor(distance: Float) {
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
        detections.onNext(detectionType)
    }

    enum class DetectionMode {
        DEFAULT, SEARCH_OBSTACLE_HEIGHT, SEARCH_OBSTACLE_COLOR, IGNORE
    }

    enum class DetectionType {
        PRECIPICE, OBSTACLE, NOTHING, ROBOT
    }
}