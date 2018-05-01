package com.angrynerds.ev3.core

import lejos.hardware.motor.Motor
import lejos.hardware.port.SensorPort

object Ports {
    var tractionMotorRight = Motor.D
    var tractionMotorLeft = Motor.A
    var grabMotor = Motor.C

    val infraredSensor = SensorPort.S3
    val colorSensorRight = SensorPort.S2
    val colorSensorForward = SensorPort.S4
    val ultrasonicSensor = SensorPort.S1
}
