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
import lejos.hardware.Sound
import lejos.hardware.lcd.LCD
import lejos.sensors.ColorId
import java.io.File
import java.util.logging.LogManager
import java.util.logging.Logger

var rootLogger = Logger.getLogger("main")
val compositeSubscription = CompositeDisposable()

fun main(args: Array<String>) {

    // TODO remove inconclusive and possible obstacles

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
    Detector.obstacles(Obstacle.STABLE).withoutAvoidingPrecipice().subscribe(::onStable)
    Detector.obstacles(Obstacle.STABLE_OPPONENT).withoutAvoidingPrecipice().subscribe(::onOpponentStable)
    Detector.obstacles(Obstacle.FEED).withoutAvoidingPrecipice().subscribe(::onFeed)
    Detector.obstacles(Obstacle.FEED_OPPONENT).withoutAvoidingPrecipice().subscribe(::onOpponentFeed)
    Detector.obstacles(Obstacle.FENCE).withoutAvoidingPrecipice().subscribe(::onFence)
    Detector.obstacles(Obstacle.TREE).withoutAvoidingPrecipice().subscribe(::onTree)
    Detector.obstacles(Obstacle.ANIMAL).withoutAvoidingPrecipice().subscribe(::onAnimal)
    Detector.obstacles(Obstacle.ROBOT).withoutAvoidingPrecipice().subscribe { onRobot() }

    Detector.detectionSubject.filter { it == DetectionType.ROBOT }.subscribe { onRobot() }
    Detector.detectionSubject.filter { it == DetectionType.PRECIPICE }.subscribe { onPrecipice() }
    Detector.start()

    FeederRobot.moveRobot()
}

private fun onPrecipice() {
    printStatusOf("onPrecipice")
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
        FeederRobot.searchMode = SearchMode.FEED
        FeederRobot.stopRobot()
        moveGripperArmTo(GripperArmPosition.BOTTOM_OPEN)
        FeederRobot.turnAround()
    } else {
        FeederRobot.turnAround()
    }
}

fun onOpponentFeed(obstacleInfo: ObstacleInfo) {
    printStatusOf("onOpponentFeed")
    FeederRobot.stopRobot(1000)
    FeederRobot.avoidObstacle()
}

fun onFeed(obstacleInfo: ObstacleInfo) {
    if (FeederRobot.searchMode == SearchMode.FEED) {
        printStatusOf("onFeed")
        FeederRobot.searchMode = SearchMode.STABLE
        FeederRobot.stopRobot()
        moveGripperArmTo(GripperArmPosition.BOTTOM_CLOSED)
        moveGripperArmTo(GripperArmPosition.STABLE)
        FeederRobot.moveRobot()
    }
}

fun onAnimal(obstacleInfo: ObstacleInfo) {
    printStatusOf("onAnimal")
    FeederRobot.stopRobot(1000)
    FeederRobot.avoidObstacle()
}

fun onTree(obstacleInfo: ObstacleInfo) {
    // TODO case when infrared sensor is on same height as tree
    printStatusOf("onTree")
    FeederRobot.stopRobot(1000)

    FeederRobot.moveRobotByDistance(50.0, false,
            Constants.Movement.HIGH_SPEED)
}

fun onFence(obstacleInfo: ObstacleInfo) {
    printStatusOf("onFence")
}

fun printStatusOf(funName: String) = rootLogger.info("$funName: " +
        "| searchMode=${FeederRobot.searchMode.name} " +
        "| mode=${FeederRobot.mode.name} " +
        "| gripperArmPosition=${FeederRobot.gripperArmPosition.name} " +
        "| obstacle= ${Detector.currentObstacleInfo?.possibleObstacles?.joinToString(",")}")

fun printStatusOf(funName: String, detectionType: DetectionType) = rootLogger.info("$funName: " +
        "| searchMode=${FeederRobot.searchMode.name} " +
        "| mode=${FeederRobot.mode.name} " +
        "| gripperArmPosition=${FeederRobot.gripperArmPosition.name} " +
        "| detectionType=$detectionType " +
        "| obstacle= ${Detector.currentObstacleInfo?.possibleObstacles?.joinToString(",")}")

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

    do {
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
    } while (!shouldQuit)

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
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe({})
    RxFeederRobot.rxInfraredSensor.distance.subscribe({})
    RxFeederRobot.rxColorSensorForward.colorId.blockingFirst()
    RxFeederRobot.rxColorSensorForward.colorId.subscribe({ println("FORWARDCOLOR=" + it.toString()) })
    RxFeederRobot.rxColorSensorVertical.colorId.blockingFirst()
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