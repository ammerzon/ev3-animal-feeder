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
    resetToInitialState()

    logger.info("Press a button to calibrate the robot...")
    Button.waitForAnyPress()
    calibrateRobot()

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
        0 -> test01PrecepiceDetection()
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
    TODO("not implemented")
}

fun calibrateRobot() {
    logger.info("Robot calibration started")
    Config.init(FeederRobot.animalType)
    TODO("not implemented")
    logger.info("Animal type: " + FeederRobot.animalType)
}

fun openConnections() {
    logger.info("Opening connections")
    FeederRobot.tractionMotorLeft
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
fun test01PrecepiceDetection() {
    TODO("not implemented")
}

/**
 * Roboter fährt auf Stein zu und stoppt kurz vor Berührung des Steines ab.
 */
fun test02ObstacleStop() {
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
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
    RxFeederRobot.rxColorSensorForward.colorId.subscribe { colorId ->
        if (isMyFeedColor(colorId.id)) {
            moveGrapplerTo(GrapplerPosition.BOTTOM_OPEN)
        }
    }
}

/**
 * Roboter schließt Greifarm und hebt Duplostein, hält ihn oben und kann ihn transportieren.
 */
fun test04CarryStone() {
    moveGrapplerTo(GrapplerPosition.BOTTOM_CLOSED)
    moveGrapplerTo(GrapplerPosition.TOP)
}

/**
 * Roboter fährt (mit gehobenen/geschlossenen) Greifarm auf kalkulierte Richtung des Geheges zu.
 */
fun test05FindStable() {
    TODO("not implemented")
}

/**
 * Baum befindet sich sich ca. 20 cm vor Roboter. Dieser erkennt Baum und schmeißt ihn um.
 */
fun test06CutTree() {
    val treeMinDistance = 100 // TODO: configure
    fun isTreeAhead(): Boolean {
        return FeederRobot.infraredSensor.getDistance() < treeMinDistance
    }

    fun cutTree() {
        // Move grappler to middle position
        moveGrapplerTo(GrapplerPosition.MIDDLE)

        val overturnDistance = 10.0 // distance which should force tree to tilt. TODO: configure
        val grapplerLength = 50.0
        val treeDistance = FeederRobot.infraredSensor.getDistance() - grapplerLength
        val travelDistance = treeDistance + overturnDistance
        FeederRobot.movePilot.travel(travelDistance, true)
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
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        if (isFence(distance)) {
            stopRobot()
            moveGrapplerTo(GrapplerPosition.MIDDLE)
            FeederRobot.movePilot.travel(5.0)
        }
    }

    moveRobot()
}

private fun moveRobot() {
    FeederRobot.movePilot.forward()
    println("robot moved forward")
}

private fun stopRobot() {
    FeederRobot.movePilot.stop()
    println("robot stopped")
}

/**
 * Dem Roboter werden unterschiedliche, relevante, Hindernisse (wie zB Baum, Zaun, Futter) vorgesetzt. Der Roboter reagiert anhand des vorgesetzten Hindernisses.
 */
fun test08ObstacleDetection() {
    TODO("not implemented")
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
    TODO("not implemented")
}


/**
 * Move grappler to a specific height.
 */
fun moveGrapplerTo(pos: GrapplerPosition) {
    val delta = FeederRobot.grapplerPosition.rotations - pos.rotations

    FeederRobot.grabMotor.speed = if (delta > 0)
        Constants.Grabber.UPWARD_SPEED
    else Constants.Grabber.UPWARD_SPEED
    FeederRobot.grabMotor.rotate(delta.toInt())

    FeederRobot.grapplerPosition = pos
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

/*fun testAvoidEnclosure() {
    print("Started testAvoidEnclosure")
    FeederRobot.movePilot.linearSpeed = FORWARD_SPEED
    FeederRobot.movePilot.forward()
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        print("UR: " + distance)
        if (distance.isFinite()) {
            if ((isUltrasonicRaised && distance > ULTRASONIC_HIGH_TABLE_DISTANCE) || (!isUltrasonicRaised && distance > ULTRASONIC_LOW_TABLE_DISTANCE)) {
                FeederRobot.movePilot.stop()
                FeederRobot.movePilot.travel(-100.0)
                FeederRobot.movePilot.rotate(90.0)
            }
            Thread.sleep(2000)
            if (!FeederRobot.movePilot.isMoving) {
                FeederRobot.movePilot.forward()
            }
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

fun testRaiseStone() {
    println("Started testRaiseStone")
    println("Place stone and press a button ...")
    Button.waitForAnyPress()
    val forwardColorId = RxFeederRobot.rxColorSensorForward.colorId.blockingFirst()
    if (forwardColorId.equals(foodColor)) {
        println("Food recognized color=${forwardColorId.name}")
        FeederRobot.grabMotor.speed = GRABBER_UPWARD_SPEED + 200
        FeederRobot.grabMotor.rotate(24000)
    } else {
        println("No food recognized color=${forwardColorId.name}")
        FeederRobot.movePilot.travel(-50.0)
    }
}*/