package com.angrynerds.ev3.core

import lejos.sensors.RxEV3ColorSensor
import lejos.sensors.RxEV3IRSensor
import lejos.sensors.RxEV3UltrasonicSensor

object RxFeederRobot {
    var infraredSensor = RxEV3IRSensor(Ports.infraredSensor, false)
    var colorSensorRight = RxEV3ColorSensor(Ports.colorSensorRight, false)
    var colorSensorForward = RxEV3ColorSensor(Ports.colorSensorForward, false)
    var ultrasonicSensor = RxEV3UltrasonicSensor(Ports.ultrasonicSensor, false)
}
