package com.angrynerds.ev3.core

import lejos.hardware.Device
import lejos.hardware.motor.EV3LargeRegulatedMotor
import lejos.hardware.motor.EV3MediumRegulatedMotor
import lejos.hardware.motor.Motor
import lejos.hardware.port.MotorPort
import lejos.hardware.port.SensorPort
import lejos.hardware.sensor.EV3ColorSensor
import lejos.hardware.sensor.EV3IRSensor
import lejos.hardware.sensor.EV3UltrasonicSensor
import lejos.hardware.sensor.NXTColorSensor
import lejos.robotics.RegulatedMotor
import lejos.robotics.chassis.WheeledChassis
import lejos.robotics.navigation.MovePilot

object FeederRobot {
    var tractionMotorRight = EV3LargeRegulatedMotor(Ports.tractionMotorRight)
    var tractionMotorLeft = EV3LargeRegulatedMotor(Ports.tractionMotorLeft)
    var grabMotor = EV3MediumRegulatedMotor(Ports.grabMotor)

    var infraredSensor = EV3IRSensor(Ports.infraredSensor)
    var colorSensorRight = EV3ColorSensor(Ports.tractionMotorRight)
    var colorSensorForward = EV3ColorSensor(Ports.colorSensorForward)
    var ultrasonicSensor = EV3UltrasonicSensor(Ports.ultrasonicSensor)

    var wheelRight = WheeledChassis.modelWheel(tractionMotorRight, 35.0).offset(172.5)
    var wheelLeft = WheeledChassis.modelWheel(tractionMotorLeft, 35.0).offset(-172.5)
    var chassis = WheeledChassis(arrayOf(wheelRight, wheelLeft), WheeledChassis.TYPE_DIFFERENTIAL)
    var movePilot = MovePilot(chassis)

    var grabberPositionAsAngle = 0

    fun close(){
        val devices = arrayOf(tractionMotorRight, tractionMotorLeft, grabMotor, infraredSensor, colorSensorRight, colorSensorForward, ultrasonicSensor)
        devices.forEach(Device::close)
    }
}