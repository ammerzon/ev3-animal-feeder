package com.angrynerds.ev3

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.RxFeederRobot
import lejos.hardware.Button
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
    TODO("not implemented")
    logger.info("Animal type: " + FeederRobot.animalType)
}

fun openConnections() {
    logger.info("Open connections")
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
    TODO("not implemented")
}

/**
 * Duplostein befindet sich direkt vor Roboter, Roboter erkennt Farbe des Futter und öffnet Greifarm.
 */
fun test03FeedColorDetection() {
    TODO("not implemented")
}

/**
 * Roboter schließt Greifarm und hebt Duplostein, hält ihn oben und kann ihn transportieren.
 */
fun test04CarryStone() {
    TODO("not implemented")
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
    TODO("not implemented")
}

/**
 * Roboter befindet sich in der Arena und stößt Zaun weg.
 */
fun test07DemolishFence() {
    TODO("not implemented")
}

/**
 * Dem Roboter werden unterschiedliche, relevante, Hindernisse (wie zB Baum, Zaun, Futter) vorgesetzt. Der Roboter reagiert anhand des vorgesetzten Hindernisses.
 */
fun test08ObstacleDetection() {
    TODO("not implemented")
}

/**
 * Der Roboter steht mit erhobenem Arm vor dem Gehege und fährt darauf zu. Wenn der Arm über dem Gehege ist soll es stoppen.
 */
fun test09MeasureHeight() {
    TODO("not implemented")
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