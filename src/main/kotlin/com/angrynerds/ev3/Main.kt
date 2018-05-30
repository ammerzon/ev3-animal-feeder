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
 * Roboter fährt auf Tischkante zu, erkennt Abgrund, wendet und fährt weiter.
 */
fun test01PrecipiceDetection() {
    println("Place robot and press a key...")
    Button.waitForAnyPress()
    FeederRobot.movePilot.linearSpeed = Constants.Movement.DEFAULT_SPEED
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        println(distance)
        if (distance.isFinite()) {
            println("Position ${FeederRobot.gripperArmPosition.isBottom()}")
            println(distance > Constants.PrecipiceDetection.ULTRASONIC_THRESHOLD_GRABBER_DOWN.endInclusive)
            if ((FeederRobot.gripperArmPosition == GripperArmPosition.TOP && distance > Constants.PrecipiceDetection.ULTRASONIC_THRESHOLD_GRABBER_UP.endInclusive) ||
                    (FeederRobot.gripperArmPosition.isBottom() && distance > Constants.PrecipiceDetection.ULTRASONIC_THRESHOLD_GRABBER_DOWN.endInclusive)) {
                stopRobot()
                Thread.sleep(1000)
                moveRobot(Constants.PrecipiceDetection.BACKWARD_TRAVEL_DISTANCE)
                rotateRobot(Constants.PrecipiceDetection.ROTATION_ANGLE)
            }
            Thread.sleep(2000)
            if (!FeederRobot.movePilot.isMoving) {
                moveRobot()
            }
        }
    }

    moveRobot()
}

/**
 * Roboter fährt auf Stein zu und stoppt kurz vor Berührung des Steines ab.
 */
fun test02ObstacleStop() {
    println("Place robot and press a key...")
    Button.waitForAnyPress()
    FeederRobot.movePilot.linearSpeed = Constants.Movement.DEFAULT_SPEED - 40.0
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        println(distance)
        if (isFeed(distance)) {
            stopRobot()
        }
    }

    moveRobot()
}

/**
 * Duplostein befindet sich direkt vor Roboter, Roboter erkennt Farbe des Futter und öffnet Greifarm.
 */
fun test03FeedColorDetection() {
    println("Place robot in front of feed and press a key...")
    Button.waitForAnyPress()
    FeederRobot.colorSensorForward.rgbMode
    FeederRobot.gripperArmPosition = GripperArmPosition.BOTTOM_CLOSED
    RxFeederRobot.rxColorSensorForward.colorId.subscribe { colorId ->
        println(colorId.name)
        if (isMyFeedColor(colorId.id)) {
            moveGripperArmTo(GripperArmPosition.BOTTOM_OPEN)
        }
    }
}

/**
 * Roboter schließt Greifarm und hebt Duplostein, hält ihn oben und kann ihn transportieren.
 */
fun test04CarryStone() {
    println("Put feed in gripper arm and press a key...")
    Button.waitForAnyPress()
    moveGripperArmTo(GripperArmPosition.BOTTOM_CLOSED)
    moveGripperArmTo(GripperArmPosition.TOP)
}

/**
 * Roboter fährt (mit gehobenen/geschlossenen) Greifarm auf kalkulierte Richtung des Geheges zu.
 */
fun test05FindStable() {
    RxFeederRobot.rxInfraredSensor.distance.subscribe { irDist ->
        if (irDist.isFinite() && irDist <= Constants.ObstacleCheck.MIN_OBSTACLE_DISTANCE) {
            if (!isStable(FeederRobot.colorSensorForward.colorID)) {
                stopRobot()
                moveRobot(Constants.PrecipiceDetection.BACKWARD_TRAVEL_DISTANCE)
                rotateRobot(Constants.ObstacleCheck.ROTATION_ANGLE)
                moveRobot()
            } else {
                stopRobot()
            }
        }
    }

    moveGripperArmTo(GripperArmPosition.TOP)
    moveRobot()
}

/**
 * Baum befindet sich sich ca. 20 cm vor Roboter. Dieser erkennt Baum und schmeißt ihn um.
 */
fun test06CutTree() {
    println("Place robot in front of tree and press a key...")
    Button.waitForAnyPress()

    // Move up, so distance can be measured
    //moveGripperArmTo(GripperArmPosition.TOP)

    val treeMinDistance = 30 // TODO: configure
    fun isTreeAhead(): Boolean {
        // Move up, so distance can be measured
        //moveGripperArmTo(GripperArmPosition.TOP)
        return true
        return FeederRobot.infraredSensor.getDistance() < treeMinDistance
    }

    fun cutTree() {
        // Move gripper arm to middle position
        moveGripperArmTo(GripperArmPosition.MIDDLE)

        val overturnDistance = 5.0 // distance which should force tree to tilt. TODO: configure
        val gripperArmLength = 50.0
        val treeDistance = treeMinDistance //FeederRobot.infraredSensor.getDistance() - gripperArmLength
        val travelDistance = 300.0

        FeederRobot.movePilot.linearSpeed = 400.0
        moveRobot(travelDistance, false)
        // TODO: not sure, if immediate return has the desired effect
    }

    // Note that color should be checked for real detection
    if (isTreeAhead())
        cutTree();
}

/**
 * Roboter befindet sich in der Arena und stößt Zaun weg.
 */
fun test07DemolishFence() {
    println("Place robot in front of fence and press a key...")
    Button.waitForAnyPress()
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        if (isFence(distance)) {
            stopRobot()
            moveGripperArmTo(GripperArmPosition.MIDDLE)
            moveRobot(5.0)
        }
    }

    moveRobot()
}

/**
 * Dem Roboter werden unterschiedliche, relevante, Hindernisse (wie zB Baum, Zaun, Futter) vorgesetzt. Der Roboter reagiert anhand des vorgesetzten Hindernisses.
 */
fun test08ObstacleDetection() {
    println("Put obstacle before infrared sensor and press a button...")
    Button.waitForAnyPress()

    moveGripperArmTo(GripperArmPosition.TOP)
    RxFeederRobot.rxInfraredSensor.distance.subscribe { irDist ->
        if (irDist.isFinite() && irDist <= Constants.ObstacleCheck.MIN_OBSTACLE_DISTANCE) {
            stopRobot()
            moveRobot(Constants.PrecipiceDetection.BACKWARD_TRAVEL_DISTANCE)
            rotateRobot(Constants.ObstacleCheck.ROTATION_ANGLE)
            moveRobot()
        }
    }

    moveRobot()
}

/**
 * Der Roboter steht mit erhobenem Arm vor dem Gehege und fährt darauf zu. Wenn der Arm über dem Gehege ist soll es stoppen.
 */
fun test10MeasureHeight() {
    moveGripperArmTo(GripperArmPosition.TOP)
    println("Place robot in front of the stable and press a key...")
    Button.waitForAnyPress()
    FeederRobot.movePilot.linearSpeed = Constants.Movement.DEFAULT_SPEED
    FeederRobot.movePilot.forward()
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        if (distance.isFinite()) {
            if (distance < Constants.StableDetection.STABLE_HEIGHT) {
                FeederRobot.movePilot.stop()
                println("Found stable")
            }
        }
    }
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

    FeederRobot.grabMotor.speed = if (delta > 0)  Constants.Grabber.UPWARD_SPEED else Constants.Grabber.DOWNWARD_SPEED
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