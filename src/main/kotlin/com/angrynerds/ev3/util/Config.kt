package com.angrynerds.ev3.util

import com.angrynerds.ev3.enums.AnimalType

object Config {
    var animalType = AnimalType.WINNIE_POOH
        get() = field
        set(value) {
            field = value
            when (field) {
                AnimalType.WINNIE_POOH -> {
                    myFeedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
                    opponentFeedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
                    stableColor = Constants.StableDetection.WINNIE_POOH_STABLE_COLOR
                    opponentStableColor = Constants.StableDetection.I_AAH_FEED_COLOR
                }
                AnimalType.I_AAH -> {
                    myFeedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
                    opponentFeedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
                    stableColor = Constants.StableDetection.I_AAH_FEED_COLOR
                    opponentStableColor = Constants.StableDetection.WINNIE_POOH_STABLE_COLOR
                }
            }
        }
    var myFeedColor = Constants.ObstacleCheck.WINNIE_POOH_FEED_COLOR
    var opponentFeedColor = Constants.ObstacleCheck.I_AAH_FEED_COLOR
    var stableColor = Constants.StableDetection.WINNIE_POOH_STABLE_COLOR
    var opponentStableColor = Constants.StableDetection.I_AAH_FEED_COLOR
}