package com.angrynerds.ev3.core

import lejos.hardware.motor.Motor
import lejos.hardware.port.SensorPort
import lejos.hardware.sensor.EV3ColorSensor
import lejos.hardware.sensor.EV3IRSensor
import lejos.hardware.sensor.EV3UltrasonicSensor
import lejos.robotics.chassis.WheeledChassis
import lejos.robotics.navigation.MovePilot

object FeederRobot {
    var tractionMotorRight = Motor.D
    var tractionMotorLeft = Motor.A
    var grabMotor = Motor.C

    var infraredSensor = EV3IRSensor(SensorPort.S1)
    var colorSensorRight = EV3ColorSensor(SensorPort.S2)
    var colorSensorForward = EV3ColorSensor(SensorPort.S4)
    var ultrasonicSensor = EV3UltrasonicSensor(SensorPort.S3)

    var wheelRight = WheeledChassis.modelWheel(tractionMotorRight, 35.0).offset(172.5)
    var wheelLeft = WheeledChassis.modelWheel(tractionMotorLeft, 35.0).offset(-172.5)
    var chassis = WheeledChassis(arrayOf(wheelRight, wheelLeft), WheeledChassis.TYPE_DIFFERENTIAL)
    var movePilot = MovePilot(chassis)
}