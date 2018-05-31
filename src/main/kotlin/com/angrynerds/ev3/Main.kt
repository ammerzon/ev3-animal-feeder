package com.angrynerds.ev3

import com.angrynerds.ev3.core.Detector
import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.RxFeederRobot
import com.angrynerds.ev3.enums.GripperArmPosition
import com.angrynerds.ev3.enums.Obstacle
import com.angrynerds.ev3.extensions.getDistance
import com.angrynerds.ev3.util.*
import io.reactivex.disposables.CompositeDisposable
import lejos.hardware.Button
import lejos.hardware.lcd.LCD
import lejos.sensors.ColorId
import java.util.logging.Logger

val logger = Logger.getLogger("main")!!
val compositeSubscription = CompositeDisposable()

fun main(args: Array<String>) {

    logger.info("Animal feeder started")
    openConnections()
    resetToInitialState()

    logger.info("Press a button to calibrate the robot...")
    Button.waitForAnyPress()
    //calibrateRobot()

    logger.info("Press a button to start execution...")
    Button.waitForAnyPress()

    // Exec
    testDetector()

    standardOutClear();
    LCD.clear()

    logger.info("Press a button to close the program...")
    Button.waitForAnyPress()
    FeederRobot.close()
}

fun testDetector() {
    logger.info("Detecting...")

    Detector.detections.subscribe {
        logger.info("detected: $it")
    }

    Button.waitForAnyPress()
}

fun resetToInitialState() {
    logger.info("Resetting to initial state")
    val disposable = RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        if (distance.isFinite()) {
            if (distance in Constants.Reset.ULTRASONIC_GRABBER_DOWN) {
                FeederRobot.grabMotor.stop()
                FeederRobot.gripperArmPosition = GripperArmPosition.BOTTOM_CLOSED
                moveGripperArmTo(GripperArmPosition.BOTTOM_OPEN)
                compositeSubscription.clear()
            }
        }
    }
    compositeSubscription.add(disposable)
    FeederRobot.grabMotor.speed = Constants.Reset.SPEED
    FeederRobot.grabMotor.backward()
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

/**
 * Opens all motor ports and sensor ports and creates the sensor observables.
 */
fun openConnections() {
    logger.info("Opening connections")
    FeederRobot.grabMotor
    RxFeederRobot.rxUltrasonicSensor
}

/**
 * Moves the gripper arm to a specific [position].
 *
 * @param position the position the gripper arm should move to.
 */
fun moveGripperArmTo(position: GripperArmPosition) {
    val delta = position.rotations - FeederRobot.gripperArmPosition.rotations

    FeederRobot.grabMotor.speed = Constants.GripperArm.SPEED
    FeederRobot.grabMotor.rotate(delta.toInt())

    FeederRobot.gripperArmPosition = position
}