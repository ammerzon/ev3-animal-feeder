package com.angrynerds.ev3.core

import com.angrynerds.ev3.enums.AnimalType
import com.angrynerds.ev3.enums.GripperArmPosition
import com.angrynerds.ev3.enums.Mode
import com.angrynerds.ev3.enums.SearchMode
import com.angrynerds.ev3.util.Constants
import lejos.hardware.Device
import lejos.hardware.motor.EV3LargeRegulatedMotor
import lejos.hardware.motor.EV3MediumRegulatedMotor
import lejos.hardware.port.MotorPort
import lejos.hardware.sensor.EV3ColorSensor
import lejos.hardware.sensor.EV3IRSensor
import lejos.hardware.sensor.EV3UltrasonicSensor
import lejos.robotics.chassis.WheeledChassis
import lejos.robotics.navigation.MovePilot

object FeederRobot {
    //region Motors
    var tractionMotorRight = EV3LargeRegulatedMotor(MotorPort.D)
    var tractionMotorLeft = EV3LargeRegulatedMotor(MotorPort.A)
    var grabMotor = EV3MediumRegulatedMotor(MotorPort.C)
    //endregion

    //region Sensors
    var infraredSensor = EV3IRSensor(Ports.infraredSensor)
    var colorSensorVertical = EV3ColorSensor(Ports.colorSensorVertical)
    var colorSensorForward = EV3ColorSensor(Ports.colorSensorForward)
    var ultrasonicSensor = EV3UltrasonicSensor(Ports.ultrasonicSensor)
    //endregion

    //region Chassis
    var wheelRight = WheeledChassis.modelWheel(tractionMotorRight, 35.0).offset(172.5)
    var wheelLeft = WheeledChassis.modelWheel(tractionMotorLeft, 35.0).offset(-172.5)
    var chassis = WheeledChassis(arrayOf(wheelRight, wheelLeft), WheeledChassis.TYPE_DIFFERENTIAL)
    var movePilot = MovePilot(chassis)
    //endregion

    //region Modes
    var animalType = AnimalType.WINNIE_POOH
        get() = field
        set(value) {
            field = value
            when (field) {
                AnimalType.WINNIE_POOH -> {
                    feedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
                    opponentFeedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
                    stableColor = Constants.StableDetection.WINNIE_POOH_STABLE_COLOR
                    opponentStableColor = Constants.StableDetection.I_AAH_FEED_COLOR
                }
                AnimalType.I_AAH -> {
                    feedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
                    opponentFeedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
                    stableColor = Constants.StableDetection.I_AAH_FEED_COLOR
                    opponentStableColor = Constants.StableDetection.WINNIE_POOH_STABLE_COLOR
                }
            }
        }
    var feedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
    var opponentFeedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
    var stableColor = Constants.StableDetection.WINNIE_POOH_STABLE_COLOR
    var opponentStableColor = Constants.StableDetection.I_AAH_FEED_COLOR
    var gripperArmPosition = GripperArmPosition.BOTTOM_OPEN
    var mode = Mode.HALTING
    var searchMode = SearchMode.FEED
    //endregion

    fun close() {
        val devices = arrayOf(tractionMotorRight, tractionMotorLeft, grabMotor, infraredSensor, colorSensorVertical, colorSensorForward, ultrasonicSensor)
        devices.forEach(Device::close)
    }

    fun moveRobot(speed: Double = Constants.Movement.DEFAULT_SPEED) {
        mode = Mode.MOVING
        movePilot.linearSpeed = speed
        movePilot.forward()
    }

    fun stopRobot(delayAfter: Long = 0) {
        movePilot.stop()
        mode = Mode.HALTING
        //Thread.sleep(delayAfter)
    }

    fun rotateRobot(angle: Double) {
        val modeBefore: Mode = FeederRobot.mode
        mode = Mode.ROTATING
        movePilot.rotate(angle)
        mode = modeBefore
    }

    fun moveRobotByDistance(distance: Double, immediateReturn: Boolean = false,
                            speed: Double = Constants.Movement.DEFAULT_SPEED) {
        val modeBefore: Mode = FeederRobot.mode
        val speedBefore = FeederRobot.movePilot.linearSpeed
        mode = Mode.MOVING
        movePilot.linearSpeed = speed
        movePilot.travel(distance, immediateReturn)
        mode = modeBefore
        FeederRobot.movePilot.linearSpeed = speedBefore
    }

    fun turnAround(moveAfterTurn: Boolean = true, delayAfterRotation: Long = 0) {
        moveRobotByDistance(Constants.PrecipiceDetection.BACKWARD_TRAVEL_DISTANCE)
        rotateRobot(Constants.PrecipiceDetection.ROTATION_ANGLE)
        //Thread.sleep(delayAfterRotation)
        if (moveAfterTurn) moveRobot()
    }

    fun avoidObstacle(delayAfterRotation: Long = 0) {
        moveRobotByDistance(Constants.PrecipiceDetection.BACKWARD_TRAVEL_DISTANCE)
        rotateRobot(Constants.ObstacleCheck.ROTATION_ANGLE)
        //Thread.sleep(delayAfterRotation)
        moveRobot()
    }
}