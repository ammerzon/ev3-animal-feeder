package com.angrynerds.ev3.core

import lejos.sensors.RxEV3ColorSensor
import lejos.sensors.RxEV3IRSensor
import lejos.sensors.RxEV3UltrasonicSensor

object RxFeederRobot {
    var infraredSensor = RxEV3IRSensor(Ports.infraredSensor)
    var colorSensorRight = RxEV3ColorSensor(Ports.colorSensorRight)
    var colorSensorForward = RxEV3ColorSensor(Ports.colorSensorForward)
    var ultrasonicSensor = RxEV3UltrasonicSensor(Ports.ultrasonicSensor)
}