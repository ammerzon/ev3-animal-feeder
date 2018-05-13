package com.angrynerds.ev3.core

import com.angrynerds.ev3.enums.*
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
    var colorSensorRight = EV3ColorSensor(Ports.colorSensorRight)
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
    var grapplerPosition = GrapplerPosition.BOTTOM_OPEN
    var mode = Mode.HALTING
    var searchMode = SearchMode.FEED
    //endregion

    fun close() {
        val devices = arrayOf(tractionMotorRight, tractionMotorLeft, grabMotor, infraredSensor, colorSensorRight, colorSensorForward, ultrasonicSensor)
        devices.forEach(Device::close)
    }
}