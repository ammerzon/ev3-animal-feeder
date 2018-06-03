package com.angrynerds.ev3

import com.angrynerds.ev3.core.Detector
import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.ObstacleInfo
import com.angrynerds.ev3.core.RxFeederRobot
import com.angrynerds.ev3.debug.EV3LogHandler
import com.angrynerds.ev3.enums.*
import com.angrynerds.ev3.util.Constants
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import lejos.hardware.Button
import lejos.hardware.lcd.LCD
import java.util.logging.LogManager
import java.util.logging.Logger

var rootLogger = Logger.getLogger("main")
val compositeSubscription = CompositeDisposable()

fun main(args: Array<String>) {

    LogManager.getLogManager().reset()
    rootLogger = LogManager.getLogManager().getLogger("main")
    rootLogger.addHandler(EV3LogHandler())

    rootLogger.info("Animal feeder started")
    openConnections()
    resetToInitialState()

    println("Press a button to calibrate the robot...")
    Button.waitForAnyPress()
    configureFeederRobot()

    println("Press a button to start execution...")
    Button.waitForAnyPress()

    // Exec
    // testDetector()

    LCD.clear()
    runRobot()

    println("Press a button to close the program...")
    Button.waitForAnyPress()
    FeederRobot.close()
}

fun Observable<ObstacleInfo>.withoutAvoidingPrecipice(): Observable<ObstacleInfo> {
    return this.filter { FeederRobot.mode != Mode.AVOIDING_PRECIPICE }
}

fun runRobot() {
    FeederRobot.mode = Mode.MOVING

    Detector.detectionSubject.subscribe { printStatusOf("detector", it) }

    Detector.obstacles(Obstacle.STABLE).withoutAvoidingPrecipice().subscribe(::onStable)
    Detector.obstacles(Obstacle.STABLE_OPPONENT).withoutAvoidingPrecipice().subscribe(::onOpponentStable)
    Detector.obstacles(Obstacle.FEED).withoutAvoidingPrecipice().subscribe(::onFeed)
    Detector.obstacles(Obstacle.FEED_OPPONENT).withoutAvoidingPrecipice().subscribe(::onOpponentFeed)
    Detector.obstacles(Obstacle.FENCE).withoutAvoidingPrecipice().subscribe(::onFence)
    Detector.obstacles(Obstacle.STABLE).withoutAvoidingPrecipice().subscribe(::onTree)
    Detector.obstacles(Obstacle.ANIMAL).withoutAvoidingPrecipice().subscribe(::onAnimal)
    Detector.obstacles(Obstacle.ROBOT).withoutAvoidingPrecipice().subscribe { onRobot() }

    Detector.detectionSubject.filter { it == DetectionType.ROBOT }.subscribe { onRobot() }
    Detector.detectionSubject.filter { it == DetectionType.PRECIPICE }.subscribe { onPrecipice() }
    Detector.obstacles.filter { it.possibleObstacles.size > 2 }.subscribe(::onInconclusiveObstacle)
    Detector.start()
}

fun onInconclusiveObstacle(obstacleInfo: ObstacleInfo) {
    if (obstacleInfo.isObstacle(Obstacle.TREE) && obstacleInfo.isObstacle(Obstacle.FENCE)
            && obstacleInfo.possibleObstacles.size == 2) {
    }

    if (obstacleInfo.areObstacles(Obstacle.FEED, Obstacle.FEED_OPPONENT)) {
        FeederRobot.mode = Mode.MOVING
        FeederRobot.movePilot.linearSpeed = Constants.Movement.SLOW_SPEED
        FeederRobot.moveRobot()
    }

    if (obstacleInfo.areObstacles(Obstacle.STABLE, Obstacle.STABLE_OPPONENT)) {
        //Detector.detectionMode = DetectionMode.SEARCH_OBSTACLE_COLOR
        FeederRobot.stopRobot()
        moveGripperArmTo(GripperArmPosition.STABLE)
        FeederRobot.moveRobot(Constants.Movement.SLOW_SPEED)    // TODO check if speed is applied instantly or if a stop is necessary
        // now stable and color should be recognized by Detector: onStable should be triggered
    }
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
    moveGripperArmTo(GripperArmPosition.BOTTOM_OPEN)
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

    FeederRobot.moveRobotByDistance(50.0, false,
            Constants.Movement.HIGH_SPEED) // TODO adjust value
}

fun onFence(obstacleInfo: ObstacleInfo) {
    printStatusOf("onFence")
}

fun printStatusOf(funName: String) = rootLogger.info("$funName: " +
        "| searchMode=${FeederRobot.searchMode.name} " +
        "| mode=${FeederRobot.mode.name} " +
        "| gripperArmPosition=${FeederRobot.gripperArmPosition.name}" +
        "| obstacle= ${Detector.currentObstacleInfo?.possibleObstacles?.joinToString(",")}")

fun printStatusOf(funName: String, detectionType: DetectionType) = rootLogger.info("$funName: " +
        "| searchMode=${FeederRobot.searchMode.name} " +
        "| mode=${FeederRobot.mode.name} " +
        "| gripperArmPosition=${FeederRobot.gripperArmPosition.name}" +
        "| detectionType=${detectionType}" +
        "| obstacle= ${Detector.currentObstacleInfo?.possibleObstacles?.joinToString(",")}")

fun testDetector() {
    rootLogger.info("Detecting...")

    Detector.detectionSubject.subscribe {
        rootLogger.info("detected: $it")
    }

    Button.waitForAnyPress()
}

/**
 * Moves the gripper arm to the position [GripperArmPosition.BOTTOM_OPEN].
 */
fun resetToInitialState() {
    rootLogger.info("Resetting to initial state")
    var isMovingUpwards = true
    FeederRobot.grabMotor.speed = Constants.Reset.SPEED

    val disposable = RxFeederRobot.rxUltrasonicSensor.distance.subscribe { distance ->
        if (distance.isFinite()) {
            if (isMovingUpwards) {
                if (distance > Constants.Reset.ULTRASONIC_GRABBER_DOWN.endInclusive) {
                    FeederRobot.grabMotor.stop()
                    isMovingUpwards = false
                    FeederRobot.grabMotor.backward()
                }
            } else {
                if (distance in Constants.Reset.ULTRASONIC_GRABBER_DOWN) {
                    FeederRobot.grabMotor.stop()
                    FeederRobot.gripperArmPosition = GripperArmPosition.BOTTOM_CLOSED
                    moveGripperArmTo(GripperArmPosition.BOTTOM_OPEN)
                    compositeSubscription.clear()
                }
            }
        }
    }

    compositeSubscription.add(disposable)
    FeederRobot.grabMotor.forward()
}

/**
 * Read the horizontal color sensor value and sets [FeederRobot.animalType].
 */
fun configureFeederRobot() {
    rootLogger.info("Robot calibration started")
    var shouldQuit = false

    /*do {
        println("Put color before the sensor and press a button...")
        Button.waitForAnyPress()
        val colorId = ColorId.colorId(FeederRobot.colorSensorForward.colorID)
        println("Recognized color: ${colorId.name}")
        if (colorId == Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR ||
                colorId == Constants.ObstacleCheck.I_AAH_FEED_COLOR) {
            if (colorId == Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR) {
                Sound.playSample(File(SoundEffects.BEAR.fileName))
                FeederRobot.feedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR

            } else if (colorId == Constants.ObstacleCheck.I_AAH_FEED_COLOR) {
                Sound.playSample(File(SoundEffects.DONKEY.fileName))
                FeederRobot.feedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
            }

            println("Press escape to reread color. Any other button to continue.")
            if (Button.waitForAnyPress() != Button.ID_ESCAPE) {
                shouldQuit = true
            }
        } else {
            Sound.playSample(File(SoundEffects.ERROR.fileName))
            println("Couldn't find any valid feed color (${Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR.name}| " +
                    "${Constants.ObstacleCheck.I_AAH_FEED_COLOR.name})")
        }
    } while (!shouldQuit)*/

    FeederRobot.feedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
    rootLogger.info("Animal type: " + FeederRobot.animalType)
    rootLogger.info("Feed color: " + FeederRobot.feedColor)
    rootLogger.info("Stable color: " + FeederRobot.stableColor)
}

/**
 * Opens all motor ports and sensor ports and creates the sensor observables.
 */
fun openConnections() {
    rootLogger.info("Opening connections")
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