package com.angrynerds.ev3.util

import com.angrynerds.ev3.enums.AnimalType
import lejos.sensors.ColorId

object Config {
    var animalType = AnimalType.WINNIE_POOH
    var myFeedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
    var opponentFeedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR

    fun init(animalType : AnimalType) {
        this.animalType = animalType
        when (animalType) {
            AnimalType.WINNIE_POOH -> {
                myFeedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
                opponentFeedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
            }
            AnimalType.I_AAH -> {
                myFeedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
                opponentFeedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
            }
        }
    }
}