package com.angrynerds.ev3.util

fun isFenceColor(colorId: Int): Boolean {
    return false // TODO
}

fun isTreeColor(colorId: Int): Boolean {
    return colorId == Constants.ObstacleCheck.TREE_COLOR.id
}

fun isAnimalColor(colorId: Int): Boolean {
    return Constants.ObstacleCheck.ANIMAL_COLORS.map { it.id }.contains(colorId)
}

fun isMyFeedColor(colorId: Int): Boolean {
    return colorId == Config.myFeedColor.id
}

fun isOpponentFeedColor(colorId: Int): Boolean {
    return colorId == Config.opponentFeedColor.id
}

fun isFence(distance: Float): Boolean {
    return distance <= Constants.ObstacleCheck.FENCE_HEIGHT && distance > Constants.ObstacleCheck.FEED_HEIGHT
}

fun isFeed(distance: Float): Boolean {
    return distance <= Constants.ObstacleCheck.FEED_HEIGHT
}