package com.angrynerds.ev3

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.RxFeederRobot
import com.angrynerds.ev3.extensions.getDistance
import io.reactivex.Observable
import lejos.hardware.Button
import lejos.sensors.RxEV3ColorSensor
import lejos.sensors.RxEV3IRSensor
import lejos.sensors.RxEV3UltrasonicSensor

const val OBSTACLE_MAX_DISTANCE = 35
const val GRABBER_MAX_ROTATION = 15000
const val ULTRASONIC_LOW_TABLE_DISTANCE = 0.031000002
const val ULTRASONIC_HIGH_TABLE_DISTANCE = 0.05
const val GRABBER_UPWARD_SPEED = 360
const val GRABBER_DOWNWARD_SPEED = 180
const val FORWARD_SPEED = 35.0

var isUltrasonicRaised = false

fun main(args: Array<String>) {


    println("Initialize robot ...")
    FeederRobot.tractionMotorLeft
    RxFeederRobot.colorSensorForward
    resetToStartPosition()
    println("Press a button to continue ...")
    Button.waitForAnyPress()

    RxEV3ColorSensor(FeederRobot.colorSensorForward).colorId.subscribe({ println("Color forward: " + it.id) })
    RxEV3ColorSensor(FeederRobot.colorSensorRight).colorId.subscribe({ println("Color right: " + it.id) })
    RxEV3IRSensor(FeederRobot.infraredSensor).distance.subscribe({ println("IR: " + it) })
    RxEV3UltrasonicSensor(FeederRobot.ultrasonicSensor).distance.subscribe({ println("US: " + it) })
    Button.waitForAnyPress()

    //SensorDebugger.startDebugServer()

    // Avoid things
    /*raiseGrabber()
    testAvoidThings()*/

    // Detect enclosure
    testAvoidEnclosure()
    
    Button.waitForAnyPress()
    FeederRobot.close()
}

fun testAvoidEnclosure() {
    FeederRobot.movePilot.linearSpeed = FORWARD_SPEED
    FeederRobot.movePilot.forward()
    RxFeederRobot.ultrasonicSensor.distance.subscribe { distance ->
        if ((isUltrasonicRaised && distance > ULTRASONIC_HIGH_TABLE_DISTANCE) || (!isUltrasonicRaised && distance > ULTRASONIC_LOW_TABLE_DISTANCE)) {
            FeederRobot.movePilot.stop()
            FeederRobot.movePilot.travel(-10.0)
            FeederRobot.movePilot.rotate(180.0)
        }
        Thread.sleep(2000)
        if (!FeederRobot.movePilot.isMoving) {
            FeederRobot.movePilot.forward()
        }
    }

}

fun testAvoidThings() {
    FeederRobot.movePilot.linearSpeed = FORWARD_SPEED
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
    isUltrasonicRaised = true
}

fun resetToStartPosition() {
    FeederRobot.grabMotor.speed = GRABBER_DOWNWARD_SPEED
    RxEV3UltrasonicSensor(FeederRobot.ultrasonicSensor, false).distance.filter({ it < ULTRASONIC_LOW_TABLE_DISTANCE }).take(1).subscribe({ FeederRobot.grabMotor.stop() })
    FeederRobot.grabMotor.backward()
    isUltrasonicRaised = false
}