package com.angrynerds.ev3

import com.angrynerds.ev3.core.FeederRobot
import lejos.hardware.Button
import lejos.hardware.Sound
import lejos.robotics.chassis.Wheel
import lejos.robotics.chassis.WheeledChassis
import lejos.robotics.navigation.MovePilot


fun main(args: Array<String>) {
    Sound.DOUBLE_BEEP

    val irSensor = FeederRobot.infraredSensor
    val distance = irSensor.distanceMode
    val sample = FloatArray(distance.sampleSize())

    val wheelLeft = WheeledChassis.modelWheel(FeederRobot.tractionMotorLeft, 30.0).offset(70.0)
    val wheelRight = WheeledChassis.modelWheel(FeederRobot.tractionMotorRight, 30.0).offset(-70.0)
    val chassis = WheeledChassis(arrayOf<Wheel>(wheelLeft, wheelRight), WheeledChassis.TYPE_DIFFERENTIAL)
    val movePilot = MovePilot(chassis)
    movePilot.linearSpeed = 35.0
    movePilot.forward()

    for (i in 1..10) {
        distance.fetchSample(sample, 0)
        println(sample.joinToString(";"))
        print("Found: " + (sample.get(0) > 30.0))
        if (sample.get(0) > 49.0 && movePilot.isMoving) {
            movePilot.stop()
        } else if (sample.get(0) <= 49.0 && !movePilot.isMoving) {
            movePilot.forward()
        }
        Thread.sleep(2000)
    }

    irSensor.close()
    FeederRobot.tractionMotorLeft.close()
    FeederRobot.tractionMotorRight.close()
    Button.waitForAnyPress()
}