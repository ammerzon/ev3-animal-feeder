package com.angrynerds.ev3

import com.angrynerds.ev3.core.Detector
import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.ObstacleInfo
import com.angrynerds.ev3.core.RxFeederRobot
import com.angrynerds.ev3.enums.GripperArmPosition
import com.angrynerds.ev3.enums.Mode
import com.angrynerds.ev3.enums.Obstacle
import com.angrynerds.ev3.enums.SearchMode
import com.angrynerds.ev3.util.Constants
import com.angrynerds.ev3.util.standardOutClear
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import lejos.hardware.Button
import lejos.hardware.lcd.LCD
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
    //testDetector()

    runRobot()

    standardOutClear();
    LCD.clear()

    logger.info("Press a button to close the program...")
    Button.waitForAnyPress()
    FeederRobot.close()
}

fun Observable<ObstacleInfo>.withoutAvoidingPrecipice(): Observable<ObstacleInfo> {
    return this.filter { FeederRobot.mode != Mode.AVOIDING_PRECIPICE }
}

fun runRobot() {
    FeederRobot.mode = Mode.MOVING  // need it?

    Detector.detections.subscribe { printStatusOf("detector") }

    Detector.obstacles(Obstacle.STABLE).withoutAvoidingPrecipice().subscribe(::onStable)
    Detector.obstacles(Obstacle.STABLE_OPPONENT).withoutAvoidingPrecipice().subscribe(::onOpponentStable)
    Detector.obstacles(Obstacle.FEED).withoutAvoidingPrecipice().subscribe(::onFeed)
    Detector.obstacles(Obstacle.FEED_OPPONENT).withoutAvoidingPrecipice().subscribe(::onOpponentFeed)
    Detector.obstacles(Obstacle.FENCE).withoutAvoidingPrecipice().subscribe(::onFence)
    Detector.obstacles(Obstacle.STABLE).withoutAvoidingPrecipice().subscribe(::onTree)
    Detector.obstacles(Obstacle.ANIMAL).withoutAvoidingPrecipice().subscribe(::onAnimal)
    Detector.obstacles(Obstacle.ROBOT).withoutAvoidingPrecipice().subscribe { onRobot() }

    Detector.detections.filter { it == Detector.DetectionType.ROBOT }.subscribe { onRobot() }
    Detector.detections.filter { it == Detector.DetectionType.PRECIPICE }.subscribe { onPrecipice() }
    Detector.obstacles.filter { it.possibleObstacles.size > 2 }.subscribe(::onInconclusiveObstacle)
}

fun onInconclusiveObstacle(obstacleInfo: ObstacleInfo) {
    if (obstacleInfo.isObstacle(Obstacle.TREE) && obstacleInfo.isObstacle(Obstacle.FENCE)
            && obstacleInfo.possibleObstacles.size == 2) {
    }

    if (obstacleInfo.areObstacles(Obstacle.FEED, Obstacle.FEED_OPPONENT)) {
        FeederRobot.mode = Mode.MOVING_SLOWLY
        FeederRobot.movePilot.linearSpeed = Constants.Movement.SLOW_SPEED
    }

    Detector.detectionMode = Detector.DetectionMode.SEARCH_OBSTACLE_COLOR
    // detector subscribers should be notified with unambiguous obstacles
}

private fun onPrecipice() {
    val modeBefore: Mode = FeederRobot.mode
    FeederRobot.mode = Mode.AVOIDING_PRECIPICE
    FeederRobot.stopRobot(1000)
    FeederRobot.turnAround(true, 2000)
    FeederRobot.mode = modeBefore
}

fun onRobot() {
    printStatusOf("onRobot")
    FeederRobot.avoidObstacle()
}

fun onOpponentStable(obstacleInfo: ObstacleInfo) {
    printStatusOf("onOpponentStable")
    FeederRobot.avoidObstacle()
}

fun onStable(obstacleInfo: ObstacleInfo) {
    printStatusOf("onStable")
    if (FeederRobot.searchMode == SearchMode.STABLE) {
        FeederRobot.stopRobot()
        moveGripperArmTo(GripperArmPosition.BOTTOM_OPEN)
        FeederRobot.turnAround()
        FeederRobot.searchMode = SearchMode.FEED
    }
}

fun onOpponentFeed(obstacleInfo: ObstacleInfo) {
    printStatusOf("onOpponentFeed")
    FeederRobot.stopRobot(1000)
    FeederRobot.avoidObstacle()
}

fun onFeed(obstacleInfo: ObstacleInfo) {
    printStatusOf("onFeed")
    if (FeederRobot.searchMode == SearchMode.FEED) {
        FeederRobot.stopRobot()
        moveGripperArmTo(GripperArmPosition.BOTTOM_CLOSED)
        // TODO: is here a delay necessary or is moveGripperArmTo blocking?
        moveGripperArmTo(GripperArmPosition.TOP)
        FeederRobot.searchMode = SearchMode.STABLE
    }
}

fun onAnimal(obstacleInfo: ObstacleInfo) {
    printStatusOf("onAnimal")
    FeederRobot.stopRobot(1000)
    FeederRobot.avoidObstacle()
}

fun onTree(obstacleInfo: ObstacleInfo) {
    printStatusOf("onTree")
    FeederRobot.stopRobot(1000)
    val modeBefore: Mode = FeederRobot.mode
    val speedBefore = FeederRobot.movePilot.linearSpeed

    FeederRobot.mode = Mode.MOVING_SLOWLY
    FeederRobot.movePilot.linearSpeed = Constants.Movement.HIGH_SPEED
    FeederRobot.moveRobot(50.0) // TODO adjust value

    FeederRobot.movePilot.linearSpeed = speedBefore
    FeederRobot.mode = modeBefore
}

fun onFence(obstacleInfo: ObstacleInfo) {
    printStatusOf("onFence")
    FeederRobot.stopRobot(1000)
    // TODO what is demolish fence?
}

fun printStatusOf(funName: String) = logger.info("$funName: " +
        "| searchMode=${FeederRobot.searchMode.name} " +
        "| mode=${FeederRobot.mode.name} " +
        "| gripperArmPosition=${FeederRobot.gripperArmPosition.name}" +
        "| detection=${Detector.detections.last(null).blockingGet().name}" +
        "| obstacle= ${Detector.currentObstacleInfo?.possibleObstacles?.joinToString(",")}")

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