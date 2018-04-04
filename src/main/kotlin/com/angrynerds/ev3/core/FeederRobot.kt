package com.angrynerds.ev3.core

import lejos.hardware.motor.Motor
import lejos.hardware.port.SensorPort
import lejos.hardware.sensor.EV3ColorSensor
import lejos.hardware.sensor.EV3IRSensor
import lejos.hardware.sensor.EV3UltrasonicSensor

object FeederRobot {
    var tractionMotorRight = Motor.D
    var tractionMotorLeft = Motor.A
    var grabMotor = Motor.C

    var infraredSensor = EV3IRSensor(SensorPort.S1)
    var colorSensorRight = EV3ColorSensor(SensorPort.S2)
    var colorSensorForward = EV3ColorSensor(SensorPort.S4)
    var ultrasonicSensor = EV3UltrasonicSensor(SensorPort.S3)
}