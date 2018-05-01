package com.angrynerds.ev3

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.core.RxFeederRobot
import com.angrynerds.ev3.extensions.getColorString
import io.reactivex.disposables.Disposable
import lejos.hardware.Sound
import lejos.robotics.Color

val MIN_DISTANCE = 10F
val SPEED = 35.0

val roboter = FeederRobot.movePilot

val infraredObservable = RxFeederRobot.infraredSensor.distance          // precipice recognition
val ultrasonicObservable = RxFeederRobot.ultrasonicSensor.distance      // obstacles recognition
val colorForwardObservable = RxFeederRobot.colorSensorForward.color     // food recognition
val colorRightObservable = RxFeederRobot.colorSensorRight.color         // enclosure recognition

lateinit var scanSubscriber: Disposable

fun main(args: Array<String>) {
    init()
    scanColor()
}

private fun startCompetition(foodColor: Color) {
    scanSubscriber.dispose()

    // initial move
    FeederRobot.movePilot.forward()

    infraredObservable.subscribe { distance ->
        if (distance <= MIN_DISTANCE) {
            Sound.twoBeeps()
            log("!! precipice recognized !!")

            roboter.stop()
            //TODO move a bit backward
            roboter.rotate(180.0)
        }
    }

    ultrasonicObservable.subscribe { distance ->
        roboter.stop()
        Sound.beep()
        log("obstacle recognized")

        val forwardColor: Color = colorForwardObservable.blockingFirst()
        if (forwardColor.equals(foodColor)) {
            log("Food recognized (distance=$distance,color=${forwardColor.getColorString()})")
            //TODO close arms and move backwards and bring it back
        } else {
            log("No food recognized (distance=$distance,color=${forwardColor.getColorString()})")
            //TODO ignore and move backwards
        }
    }

    colorForwardObservable.subscribe { color ->
        log("Color forward recognized: " + color.getColorString())
    }

    colorRightObservable.subscribe { color ->
        log("Color right recognized: " + color.getColorString())
    }
}

fun init() {
    FeederRobot.movePilot.linearSpeed = SPEED
}

fun log(message: String) {
    println(message)
    TODO("change to log4j")
}

fun scanColor() {
    scanSubscriber = colorForwardObservable.subscribe { color ->
        Sound.beep()
        log("Color scanned: ${color.getColorString()}")

        startCompetition(color)
    }
}
