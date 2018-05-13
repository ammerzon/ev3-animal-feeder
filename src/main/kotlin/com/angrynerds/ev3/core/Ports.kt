package com.angrynerds.ev3.core

import lejos.hardware.port.MotorPort
import lejos.hardware.port.SensorPort

object Ports {
    //region Motors
    var tractionMotorLeft = MotorPort.A
    var grabMotor = MotorPort.C
    var tractionMotorRight = MotorPort.D
    //endregion

    //region Sensors
    val colorSensorForward = SensorPort.S1
    val infraredSensor = SensorPort.S2
    val ultrasonicSensor = SensorPort.S3
    val colorSensorRight = SensorPort.S4
    //endregion
}
