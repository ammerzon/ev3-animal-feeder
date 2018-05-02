package com.angrynerds.ev3.core

import lejos.hardware.motor.Motor
import lejos.hardware.port.MotorPort
import lejos.hardware.port.SensorPort

object Ports {
    var tractionMotorRight = MotorPort.D
    var tractionMotorLeft = MotorPort.A
    var grabMotor = MotorPort.C

    val infraredSensor = SensorPort.S2
    val colorSensorRight = SensorPort.S4
    val colorSensorForward = SensorPort.S1
    val ultrasonicSensor = SensorPort.S3
}
