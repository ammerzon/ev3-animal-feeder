package com.angrynerds.ev3

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.RxFeederRobot
import com.angrynerds.ev3.enums.GrapplerPosition
import com.angrynerds.ev3.enums.Obstacle
import com.angrynerds.ev3.enums.Sound.CRACK_KID
import com.angrynerds.ev3.enums.Sound.ERROR
import com.angrynerds.ev3.extensions.getDistance
import com.angrynerds.ev3.util.*
import lejos.hardware.Button
import lejos.hardware.Sound
import lejos.hardware.lcd.LCD
import lejos.sensors.ColorId
import lejos.utility.TextMenu
import java.io.File
import java.util.logging.Logger

val logger = Logger.getLogger("main")!!

fun main(args: Array<String>) {

    logger.info("Animal feeder started")
    openConnections()
    //resetToInitialState()

    logger.info("Press a button to calibrate the robot...")
    Button.waitForAnyPress()
    //calibrateRobot()

    logger.info("Press a button to start execution...")
    Button.waitForAnyPress()

    standardOutClear();
    LCD.clear()
    val textMenu = TextMenu(arrayOf("1.1 Abgrunderkennung", "1.2 Stehen bleiben", "1.3 Futter-Farberkennung",
            "1.4 Steintransport", "1.5 Gehegefindung", "1.6 Baumbeseitigung", "1.7 Zaunbeseitigung", "1.8 Hindernisserkennung",
            "1.9 Kalibrierung", "1.10 Höhenmessung"), 1, "Test cases")
    val selectedTest = textMenu.select()

    standardOutClear();
    LCD.clear()
    when (selectedTest) {
        0 -> test01PrecipiceDetection()
        1 -> test02ObstacleStop()
        2 -> test03FeedColorDetection()
        3 -> test04CarryStone()
        4 -> test05FindStable()
        5 -> test06CutTree()
        6 -> test07DemolishFence()
        7 -> test08ObstacleDetection()
        8 -> test09Calibration()
        9 -> test10MeasureHeight()
    }

    logger.info("Press a button to close the program...")
    Button.waitForAnyPress()
    FeederRobot.close()
}

fun resetToInitialState() {
    logger.info("Resetting to initial state")

    if (FeederRobot.ultrasonicSensor.getDistance() > Constants.Reset.ULTRASONIC_THRESHOLD_GRABBER_DOWN.endInclusive) {
        FeederRobot.grapplerPosition = GrapplerPosition.TOP
        moveGrapplerTo(GrapplerPosition.BOTTOM_CLOSED)
    }
}

fun calibrateRobot() {
    logger.info("Robot calibration started")
    Config.init(FeederRobot.animalType)
    TODO("not implemented")
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
            println("Position ${FeederRobot.grapplerPosition.isBottom()}")
            println(distance > Constants.PrecipiceDetection.ULTRASONIC_THRESHOLD_GRABBER_DOWN.endInclusive)
            if ((FeederRobot.grapplerPosition == GrapplerPosition.TOP && distance > Constants.PrecipiceDetection.ULTRASONIC_THRESHOLD_GRABBER_UP.endInclusive) ||
                    (FeederRobot.grapplerPosition.isBottom() && distance > Constants.PrecipiceDetection.ULTRASONIC_THRESHOLD_GRABBER_DOWN.endInclusive)) {
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
    FeederRobot.grapplerPosition = GrapplerPosition.BOTTOM_CLOSED
    RxFeederRobot.rxColorSensorForward.colorId.subscribe { colorId ->
        println(colorId.name)
        if (isMyFeedColor(colorId.id)) {
            moveGrapplerTo(GrapplerPosition.BOTTOM_OPEN)
        }
    }
}

/**
 * Roboter schließt Greifarm und hebt Duplostein, hält ihn oben und kann ihn transportieren.
 */
fun test04CarryStone() {
    println("Put feed in grappler and press a key...")
    Button.waitForAnyPress()
    moveGrapplerTo(GrapplerPosition.BOTTOM_CLOSED)
    moveGrapplerTo(GrapplerPosition.TOP)
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

    moveGrapplerTo(GrapplerPosition.TOP)
    moveRobot()
}

/**
 * Baum befindet sich sich ca. 20 cm vor Roboter. Dieser erkennt Baum und schmeißt ihn um.
 */
fun test06CutTree() {
    println("Place robot in front of tree and press a key...")
    Button.waitForAnyPress()

    // Move up, so distance can be measured
    moveGrapplerTo(GrapplerPosition.TOP)

    val treeMinDistance = 28 // TODO: configure
    fun isTreeAhead(): Boolean {
        // Move up, so distance can be measured
        moveGrapplerTo(GrapplerPosition.TOP)

        return FeederRobot.infraredSensor.getDistance() < treeMinDistance
    }

    fun cutTree() {
        // Move grappler to middle position
        moveGrapplerTo(GrapplerPosition.MIDDLE)

        val overturnDistance = 3.0 // distance which should force tree to tilt. TODO: configure
        val grapplerLength = 50.0
        val treeDistance = treeMinDistance //FeederRobot.infraredSensor.getDistance() - grapplerLength
        val travelDistance = treeDistance + overturnDistance
        moveRobot(travelDistance, true)
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
            moveGrapplerTo(GrapplerPosition.MIDDLE)
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

    moveGrapplerTo(GrapplerPosition.TOP)
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
 * Danach werden verschiedene Farben vor den Farbsensor gehalten und bei einer Übereinstimmung zur eingelesenen Farbe wird ein Signal abgegeben.
 */
fun test09Calibration() {
    var shouldQuit = false

    println("Put reference color before the sensor and press a button...")
    Button.waitForAnyPress()
    val referenceColorId = ColorId.colorId(FeederRobot.colorSensorForward.colorID)
    println("Reference color: ${referenceColorId.name}")
    do {
        println("Place another color before the sensor and press a button (escape to quit)...")
        if (Button.waitForAnyPress() != Button.ID_ESCAPE) {
            val forwardColorId = ColorId.colorId(FeederRobot.colorSensorForward.colorID)
            if (referenceColorId == forwardColorId) {
                Sound.playSample(File(CRACK_KID.fileName))
            } else {
                Sound.playSample(File(ERROR.fileName))
            }
            println("Detected color: ${forwardColorId.name}")
        } else {
            shouldQuit = true
        }
    } while (!shouldQuit)
}

/**
 * Der Roboter steht mit erhobenem Arm vor dem Gehege und fährt darauf zu. Wenn der Arm über dem Gehege ist soll es stoppen.
 */
fun test10MeasureHeight() {
    moveGrapplerTo(GrapplerPosition.TOP)
    println("Place robot in front of the stable and press a key...")
    Button.waitForAnyPress()
    FeederRobot.movePilot.linearSpeed = Constants.Movement.DEFAULT_SPEED
    FeederRobot.movePilot.forward()
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        if (distance.isFinite()) {
            if (distance < Constants.StableDetection.ULTRASONIC_THRESHOLD) {
                FeederRobot.movePilot.stop()
                println("Found stable")
            }
        }
    }
}

/**
 * Move grappler to a specific height.
 */
fun moveGrapplerTo(position: GrapplerPosition) {
    val delta = position.rotations - FeederRobot.grapplerPosition.rotations
    println(FeederRobot.grapplerPosition.rotations)
    println(position.rotations)
    println("Delta: $delta")

    FeederRobot.grabMotor.speed = if (delta > 0)  Constants.Grabber.UPWARD_SPEED else Constants.Grabber.DOWNWARD_SPEED
    FeederRobot.grabMotor.rotate(delta.toInt())

    FeederRobot.grapplerPosition = position
}

fun getPossibleObstacles(color: ColorId, height: Double = 0.0): Array<Obstacle> {
    var possibleObstacles = arrayOf<Obstacle>()
    if (isFenceColor(color.id))
        possibleObstacles += Obstacle.FENCE
    if (isTreeColor(color.id))
        possibleObstacles += Obstacle.TREE
    if (isAnimalColor(color.id))
        possibleObstacles += Obstacle.ANIMAL
    if (isMyFeedColor(color.id))
        possibleObstacles += Obstacle.FEED
    if (isOpponentFeedColor(color.id))
        possibleObstacles += Obstacle.FEED_OPPONENT

    return possibleObstacles
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
    println(("robot travelled $distance (immediate return = $immediateReturn)"))
}