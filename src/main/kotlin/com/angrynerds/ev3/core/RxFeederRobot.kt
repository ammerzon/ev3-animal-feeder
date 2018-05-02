package com.angrynerds.ev3.core

import lejos.sensors.RxEV3ColorSensor
import lejos.sensors.RxEV3IRSensor
import lejos.sensors.RxEV3UltrasonicSensor

object RxFeederRobot {
    var rxInfraredSensor = RxEV3IRSensor(FeederRobot.infraredSensor, false)
    var rxColorSensorRight = RxEV3ColorSensor(FeederRobot.colorSensorRight, false)
    var rxColorSensorForward = RxEV3ColorSensor(FeederRobot.colorSensorForward, false)
    var rxUltrasonicSensor = RxEV3UltrasonicSensor(FeederRobot.ultrasonicSensor, false)
}
