package com.angrynerds.ev3

import com.angrynerds.ev3.core.Detector
import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.RxFeederRobot
import com.angrynerds.ev3.debug.EV3LogHandler
import com.angrynerds.ev3.enums.*
import com.angrynerds.ev3.extensions.isValidFeedColor
import com.angrynerds.ev3.util.Constants
import io.reactivex.disposables.CompositeDisposable
import lejos.hardware.Button
import lejos.hardware.Sound
import lejos.hardware.lcd.LCD
import java.io.File
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

    println("Press a button to scan feed...")
    Button.waitForAnyPress()
    scan()
}

fun scan() {
//    Detector.obstacles.filter { it == Obstacle.SCANNED_FEED }.filterProcesses().subscribe { onFeedScanned() }
    Detector.startScan()
    while (!Detector.scannedColorId.isValidFeedColor()) {
    }
    onFeedScanned()
}

fun onFeedScanned() {
    LCD.clear()
    rootLogger.info("Feed scanned: ${FeederRobot.feedColor} for ${FeederRobot.animalType.name}")
    rootLogger.info("Press escape to read the color again or press up to confirm color")

    when (FeederRobot.animalType) {
        AnimalType.WINNIE_POOH -> Sound.playSample(File(SoundEffects.BEAR.fileName))
        AnimalType.I_AAH -> Sound.playSample(File(SoundEffects.DONKEY.fileName))
    }

    println("Press button to continue...")
    val buttonResult = Button.waitForAnyPress()

    when (buttonResult) {
        Button.ID_ESCAPE -> scan()
        Button.ID_LEFT -> {
            FeederRobot.feedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
            rootLogger.info("I Aah (Green) is set as feed color.")
            run()
        }
        Button.ID_RIGHT -> {
            FeederRobot.feedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
            rootLogger.info("Winnie Pooh (Yellow) is set as feed color.")
            run()
        }
        else -> run()
    }
}

fun run() {
    LCD.clear()
    println("Press a button to start execution...")
    Button.waitForAnyPress()

    Detector.obstacles.filter { it == Obstacle.STABLE }.subscribe { onStable() }
    Detector.obstacles.filter { it == Obstacle.STABLE_OPPONENT }.subscribe { onOpponentStable() }
    Detector.obstacles.filter { it == Obstacle.STABLE_HEIGHT }.subscribe { onStableHeight() }
    Detector.obstacles.filter { it == Obstacle.FEED }.subscribe { onFeed() }
    Detector.obstacles.filter { it == Obstacle.FEED_OPPONENT }.subscribe { onOpponentFeed() }
    Detector.obstacles.filter { it == Obstacle.FENCE }.subscribe { onFence() }
    Detector.obstacles.filter { it == Obstacle.TREE }.subscribe { onTree() }
    Detector.obstacles.filter { it == Obstacle.ANIMAL }.subscribe { onAnimal() }
    Detector.obstacles.filter { it == Obstacle.ROBOT }.subscribe { onRobot() }
    Detector.obstacles.filter { it == Obstacle.PRECIPICE }.subscribe { onPrecipice() }

    Detector.startDetectingObstacles()
    FeederRobot.moveRobot()

    println("Press a button to close the program...")
    Button.waitForAnyPress()
    FeederRobot.close()
}

fun onStableHeight() {
    FeederRobot.access {
        printStatusOf("onStableHeight")
        FeederRobot.stopRobot()
        FeederRobot.moveRobot(Constants.Movement.SLOW_SPEED)
        FeederRobot.mode = Mode.APPROACHING_STABLE
    }
}

private fun onPrecipice() {
    FeederRobot.access {
        printStatusOf("onPrecipice")
        FeederRobot.avoidPrecipice()
    }
}

fun onRobot() {
    FeederRobot.access {
        printStatusOf("onRobot")
        FeederRobot.avoidObstacle()
    }
}

fun onOpponentStable() {
    FeederRobot.access {
        printStatusOf("onOpponentStable")
        FeederRobot.avoidObstacle()
    }
}

fun onStable() {
    FeederRobot.access {
        printStatusOf("onStable")
        if (FeederRobot.searchMode == SearchMode.STABLE) {
            if (FeederRobot.mode == Mode.APPROACHING_STABLE) {
                FeederRobot.searchMode = SearchMode.FEED
                FeederRobot.stopRobot()
                moveGripperArmTo(GripperArmPosition.BOTTOM_OPEN)
                moveGripperArmTo(GripperArmPosition.STABLE)
                FeederRobot.moveRobotByDistance(1.5 * Constants.PrecipiceDetection.BACKWARD_TRAVEL_DISTANCE)
                moveGripperArmTo(GripperArmPosition.BOTTOM_OPEN)
                FeederRobot.turnAround()
            } else {
                // should not happen!
                rootLogger.warning("Stable found but robot did not approach stable before")
            }
        } else {
            // stable detected, but no feed picked up before
            FeederRobot.turnAround()
        }
    }
}

fun onOpponentFeed() {
    FeederRobot.access {
        printStatusOf("onOpponentFeed")
        Sound.playSample(File(SoundEffects.YHAC.fileName))
        FeederRobot.stopRobot(1000)
        FeederRobot.avoidObstacle()
    }
}

fun onFeed() {
    FeederRobot.access {
        printStatusOf("onFeed")
        if (FeederRobot.searchMode == SearchMode.FEED) {
            Sound.playSample(File(SoundEffects.getRandomSuccessSound().fileName))

            FeederRobot.searchMode = SearchMode.STABLE

            FeederRobot.stopRobot()
            moveGripperArmTo(GripperArmPosition.BOTTOM_CLOSED)
            moveGripperArmTo(GripperArmPosition.STABLE)
            FeederRobot.moveRobot()
        }
    }
}

fun onAnimal() {
    FeederRobot.access {
        printStatusOf("onAnimal")
        FeederRobot.stopRobot(1000)
        FeederRobot.avoidObstacle()
    }
}

fun onTree() {
    // TODO case when infrared sensor is on same height as tree
    FeederRobot.access {
        printStatusOf("onTree")
        FeederRobot.stopRobot(1000)

        FeederRobot.moveRobotByDistance(50.0, false,
                Constants.Movement.HIGH_SPEED)
    }

    // TODO test if this works with high speed movement into tree - otherwise do nothing like on a fence
}

fun onFence() {
    printStatusOf("onFence")
    // do nothing special
}

fun printStatusOf(funName: String) = rootLogger.info("$funName: " +
        "| searchMode=${FeederRobot.searchMode.name} " +
        "| action=${FeederRobot.action.name} " +
        "| mode=${FeederRobot.mode.name} " +
        "| gripperArmPosition=${FeederRobot.gripperArmPosition.name} ")

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
 * Opens all motor ports and sensor ports and creates the sensor observables.
 */
fun openConnections() {
    rootLogger.info("Opening connections")
    FeederRobot.grabMotor
    RxFeederRobot.rxUltrasonicSensor.distance.subscribe { }
    RxFeederRobot.rxInfraredSensor.distance.subscribe { }
    RxFeederRobot.rxColorSensorForward.colorId.subscribe { }
    RxFeederRobot.rxColorSensorVertical.colorId.subscribe { }
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