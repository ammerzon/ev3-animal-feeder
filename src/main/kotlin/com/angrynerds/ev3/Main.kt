package com.angrynerds.ev3

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.debug.SensorDebugger
import com.angrynerds.ev3.extensions.getDistance
import io.reactivex.Observable
import lejos.hardware.Button
import lejos.hardware.Sound
import lejos.robotics.chassis.Wheel
import lejos.robotics.chassis.WheeledChassis
import lejos.robotics.navigation.MovePilot
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    Sound.DOUBLE_BEEP

    SensorDebugger.startDebugServer()

    val wheelLeft = WheeledChassis.modelWheel(FeederRobot.tractionMotorLeft, 30.0).offset(70.0)
    val wheelRight = WheeledChassis.modelWheel(FeederRobot.tractionMotorRight, 30.0).offset(-70.0)
    val chassis = WheeledChassis(arrayOf<Wheel>(wheelLeft, wheelRight), WheeledChassis.TYPE_DIFFERENTIAL)
    val movePilot = MovePilot(chassis)
    movePilot.linearSpeed = 35.0
    movePilot.forward()

    for (i in 1..10) {
        val infraredSensor = FeederRobot.infraredSensor.getDistance()
        print("Found: " + (infraredSensor > 30.0))
        if (infraredSensor > 49.0 && movePilot.isMoving) {
            movePilot.stop()
        } else if (infraredSensor <= 49.0 && !movePilot.isMoving) {
            movePilot.forward()
        }
        Thread.sleep(2000)
    }

    FeederRobot.tractionMotorLeft.close()
    FeederRobot.tractionMotorRight.close()
    Button.waitForAnyPress()
}

fun myTest() {
    Button.waitForAnyPress()

    FeederRobot.movePilot.rotateRight()
    FeederRobot.ultrasonicSensor.getDistance()

    val distanceObservable = Observable.interval(40L, TimeUnit.MILLISECONDS)
            .map { FeederRobot.ultrasonicSensor.getDistance() }

    distanceObservable.subscribe(::println)

    FeederRobot.close()

}