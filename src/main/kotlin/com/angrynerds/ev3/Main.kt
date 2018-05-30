package com.angrynerds.ev3

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.RxFeederRobot
import com.angrynerds.ev3.enums.GripperArmPosition
import com.angrynerds.ev3.enums.Obstacle
import com.angrynerds.ev3.extensions.getDistance
import com.angrynerds.ev3.util.*
import lejos.hardware.Button
import lejos.hardware.lcd.LCD
import lejos.sensors.ColorId
import java.util.logging.Logger

val logger = Logger.getLogger("main")!!

fun main(args: Array<String>) {

    logger.info("Animal feeder started")
    openConnections()
    resetToInitialState()

    logger.info("Press a button to calibrate the robot...")
    Button.waitForAnyPress()
    //calibrateRobot()

    logger.info("Press a button to start execution...")
    Button.waitForAnyPress()

    standardOutClear();
    LCD.clear()

    logger.info("Press a button to close the program...")
    Button.waitForAnyPress()
    FeederRobot.close()
}

fun resetToInitialState() {
    logger.info("Resetting to initial state")
    moveGripperArmTo(GripperArmPosition.BOTTOM_OPEN)
}

fun calibrateRobot() {
    logger.info("Robot calibration started")
    var shouldQuit = false

//    println("Put reference color before the sensor and press a button...")
//    Button.waitForAnyPress()
//    val referenceColorId = ColorId.colorId(FeederRobot.colorSensorForward.colorID)
//    println("Reference color: ${referenceColorId.name}")
//    do {
//        println("Place another color before the sensor and press a button (escape to quit)...")
//        if (Button.waitForAnyPress() != Button.ID_ESCAPE) {
//            val forwardColorId = ColorId.colorId(FeederRobot.colorSensorForward.colorID)
//            if (referenceColorId == forwardColorId) {
//                Sound.playSample(File(CRACK_KID.fileName))
//            } else {
//                Sound.playSample(File(ERROR.fileName))
//            }
//            println("Detected color: ${forwardColorId.name}")
//        } else {
//            shouldQuit = true
//        }
//    } while (!shouldQuit)
//
//    Config.init(FeederRobot.animalType)

    logger.info("Animal type: " + FeederRobot.animalType)
}

fun openConnections() {
    logger.info("Opening connections")
    FeederRobot.grabMotor
    RxFeederRobot.rxUltrasonicSensor
    RxFeederRobot.rxColorSensorForward
}

fun initializeLogging() {
    logger.info("Initialize sensor values logging")
    RxFeederRobot.rxColorSensorForward.colorId.subscribe({ println("Color forward: " + it) })
    RxFeederRobot.rxColorSensorRight.colorId.subscribe({ println("Color right: " + it) })
    RxFeederRobot.rxInfraredSensor.distance.subscribe({ println("IR: " + it) })
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe({ println("US: " + it) })
}

/**
 * Moves the gripper arm to a specific [position].
 *
 * @param position the position the gripper arm should move to.
 */
fun moveGripperArmTo(position: GripperArmPosition) {
    val delta = position.rotations - FeederRobot.gripperArmPosition.rotations
    println(FeederRobot.gripperArmPosition.rotations)
    println(position.rotations)
    println("Delta: $delta")

    FeederRobot.grabMotor.speed = if (delta > 0) Constants.GripperArm.UPWARD_SPEED else Constants.GripperArm.DOWNWARD_SPEED
    FeederRobot.grabMotor.rotate(delta.toInt())

    FeederRobot.gripperArmPosition = position
}

private fun moveRobot() {
    FeederRobot.movePilot.forward()
}

private fun stopRobot() {
    FeederRobot.movePilot.stop()
}

fun rotateRobot(angle: Double) {
    FeederRobot.movePilot.rotate(angle)
}

fun moveRobot(distance: Double, immediateReturn: Boolean = false) {
    FeederRobot.movePilot.travel(distance, immediateReturn)
}