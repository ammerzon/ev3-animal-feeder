package com.angrynerds.ev3

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.RxFeederRobot
import com.angrynerds.ev3.extensions.getDistance
import io.reactivex.Observable
import lejos.hardware.Button
import lejos.sensors.ColorId
import lejos.sensors.RxEV3ColorSensor
import lejos.sensors.RxEV3IRSensor
import lejos.sensors.RxEV3UltrasonicSensor

const val OBSTACLE_MAX_DISTANCE = 35
const val GRABBER_MAX_ROTATION = 15000
const val ULTRASONIC_LOW_TABLE_DISTANCE = 0.031000002
const val ULTRASONIC_HIGH_TABLE_DISTANCE = 0.2
const val GRABBER_UPWARD_SPEED = 400
const val GRABBER_DOWNWARD_SPEED = 200
const val FORWARD_SPEED = 35.0

var isUltrasonicRaised = false
var foodColor = ColorId.YELLOW

fun main(args: Array<String>) {


    println("Initialize robot ...")
    FeederRobot.tractionMotorLeft
    RxFeederRobot.rxColorSensorForward
    resetToStartPosition()
    println("Press a button to continue ...")
    Button.waitForAnyPress()

    initLogging()
    Button.waitForAnyPress()

    //SensorDebugger.startDebugServer()

    // Avoid things
    raiseGrabber()
    testAvoidThings()

    // Avoid enclosure
    testAvoidEnclosure()

    // Raise stone
    testRaiseStone()

    Button.waitForAnyPress()
    FeederRobot.close()
}

fun initLogging() {
    RxFeederRobot.rxColorSensorForward.colorId.subscribe({ println("Color forward: " + it) })
    RxFeederRobot.rxColorSensorRight.colorId.subscribe({ println("Color right: " + it) })
    RxFeederRobot.rxInfraredSensor.distance.subscribe({ println("IR: " + it) })
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe({ println("US: " + it) })
}

fun testAvoidEnclosure() {
    print("Started testAvoidEnclosure")
    FeederRobot.movePilot.linearSpeed = FORWARD_SPEED
    FeederRobot.movePilot.forward()
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        if ((isUltrasonicRaised && distance > ULTRASONIC_HIGH_TABLE_DISTANCE) || (!isUltrasonicRaised && distance > ULTRASONIC_LOW_TABLE_DISTANCE)) {
            FeederRobot.movePilot.stop()
            FeederRobot.movePilot.travel(-100.0)
            FeederRobot.movePilot.rotate(180.0)
        }
        Thread.sleep(2000)
        if (!FeederRobot.movePilot.isMoving) {
            FeederRobot.movePilot.forward()
        }
    }

}

fun testAvoidThings() {
    print("Started testAvoidThings")
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
    FeederRobot.grabMotor.backward()
    RxEV3UltrasonicSensor(FeederRobot.ultrasonicSensor, false).distance.filter({ it < ULTRASONIC_LOW_TABLE_DISTANCE }).take(1).subscribe({ FeederRobot.grabMotor.stop() })
    isUltrasonicRaised = false
}

fun testRaiseStone() {
    print("Started testRaiseStone")
    Button.waitForAnyPress()
    val forwardColorId = RxFeederRobot.rxColorSensorForward.colorId.blockingFirst()
    if (forwardColorId.equals(foodColor)) {
        println("Food recognized color=${forwardColorId.name}")
        raiseGrabber()
    } else {
        println("No food recognized color=${forwardColorId.name}")
        FeederRobot.movePilot.travel(-50.0)
    }
}