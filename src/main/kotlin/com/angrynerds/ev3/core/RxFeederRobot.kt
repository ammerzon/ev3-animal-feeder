package com.angrynerds.ev3.core

import com.angrynerds.ev3.lejos.hardware.sensors.RxEV3ColorSensor
import com.angrynerds.ev3.lejos.hardware.sensors.RxEV3IRSensor
import com.angrynerds.ev3.lejos.hardware.sensors.RxEV3UltrasonicSensor

object RxFeederRobot {
    var rxInfraredSensor = RxEV3IRSensor(FeederRobot.infraredSensor, false)
    var rxColorSensorVertical = RxEV3ColorSensor(FeederRobot.colorSensorVertical, false)
    var rxColorSensorForward = RxEV3ColorSensor(FeederRobot.colorSensorForward, false)
    var rxUltrasonicSensor = RxEV3UltrasonicSensor(FeederRobot.ultrasonicSensor, false)
}
