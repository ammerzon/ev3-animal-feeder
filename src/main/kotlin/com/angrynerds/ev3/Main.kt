package com.angrynerds.ev3

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.extensions.getDistance
import io.reactivex.Observable
import lejos.hardware.Button
import lejos.sensors.RxEV3ColorSensor
import lejos.sensors.RxEV3IRSensor
import lejos.sensors.RxEV3UltrasonicSensor

const val OBSTACLE_MAX_DISTANCE = 35
const val GRABBER_MAX_ROTATION = 15000
const val ULTRASONIC_TABLE_DISTANCE = 0.031000002
const val GRABBER_UPWARD_SPEED = 360
const val GRABBER_DOWNWARD_SPEED = 180

fun main(args: Array<String>) {

    println("Initialize robot ...")
    FeederRobot.infraredSensor
    resetToStartPosition()
    println("Press a button to continue ...")
    Button.waitForAnyPress()

    RxEV3ColorSensor(FeederRobot.colorSensorForward).colorId.subscribe({ println("Color forward: " + it.id) })
    RxEV3ColorSensor(FeederRobot.colorSensorRight).colorId.subscribe({ println("Color right: " + it.id) })
    RxEV3IRSensor(FeederRobot.infraredSensor).distance.subscribe({ println("IR: " + it) })
    RxEV3UltrasonicSensor(FeederRobot.ultrasonicSensor).distance.subscribe({ println("US: " + it) })
    Button.waitForAnyPress()

    //SensorDebugger.startDebugServer()
    raiseGrabber()
    testAvoidThings()

    Button.waitForAnyPress()
    FeederRobot.close()
}

fun testAvoidThings() {
    FeederRobot.movePilot.linearSpeed = 35.0
    FeederRobot.movePilot.forward()
    obstacles().subscribe { x -> onObstacle(x) }
}

fun onObstacle(obstacleDistance: Float) {

    if (FeederRobot.infraredSensor.getDistance() > OBSTACLE_MAX_DISTANCE)
        return

    dodgeObstacle()
}

fun dodgeObstacle() {
    FeederRobot.movePilot.rotate(20.0)
    FeederRobot.movePilot.forward()
}

fun obstacles(): Observable<Float> {
    return RxEV3IRSensor(FeederRobot.infraredSensor).distance
            .filter { distance -> distance <= OBSTACLE_MAX_DISTANCE }
}

fun raiseGrabber() {
    FeederRobot.grabMotor.speed = GRABBER_UPWARD_SPEED
    FeederRobot.grabMotor.rotate(GRABBER_MAX_ROTATION - FeederRobot.grabberPositionAsAngle)
    FeederRobot.grabberPositionAsAngle = GRABBER_MAX_ROTATION
}

fun resetToStartPosition() {
    FeederRobot.grabMotor.speed = GRABBER_DOWNWARD_SPEED
    RxEV3UltrasonicSensor(FeederRobot.ultrasonicSensor, false).distance.filter({ it < ULTRASONIC_TABLE_DISTANCE }).take(1).subscribe({ FeederRobot.grabMotor.stop() })
    FeederRobot.grabMotor.backward()
}