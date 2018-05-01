package com.angrynerds.ev3.core

import lejos.hardware.Device
import lejos.robotics.chassis.WheeledChassis
import lejos.robotics.navigation.MovePilot

object FeederRobot {
    var wheelRight = WheeledChassis.modelWheel(Ports.tractionMotorRight, 35.0).offset(172.5)
    var wheelLeft = WheeledChassis.modelWheel(Ports.tractionMotorLeft, 35.0).offset(-172.5)
    var chassis = WheeledChassis(arrayOf(wheelRight, wheelLeft), WheeledChassis.TYPE_DIFFERENTIAL)
    var movePilot = MovePilot(chassis)

    fun close() {
        arrayOf(Ports.tractionMotorRight, Ports.tractionMotorLeft, Ports.grabMotor)
                .forEach(Device::close)
    }
}