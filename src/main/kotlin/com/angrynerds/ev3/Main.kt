package com.angrynerds.ev3

import lejos.hardware.Button
import lejos.hardware.motor.Motor

fun main(args: Array<String>) {
    print("Hello world!")
    Button.waitForAnyPress()
    Motor.A.forward()
}