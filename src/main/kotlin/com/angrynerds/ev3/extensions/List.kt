package com.angrynerds.ev3.extensions

import java.util.*

fun <E> List<E>.getRandomElement() = this[Random().nextInt(this.size)]