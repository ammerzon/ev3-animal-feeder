package com.angrynerds.ev3.core

import com.angrynerds.ev3.enums.Obstacle
import com.angrynerds.ev3.util.*
import lejos.sensors.ColorId

class ObstacleInfo {
    var colors = emptyArray<ColorId>()

    var minHeight : Float = 0f
    var maxHeight : Float = Float.POSITIVE_INFINITY
    val heightRange : ClosedFloatingPointRange<Float>
        get() = minHeight..maxHeight

    fun detectedHeight(height: Float) {
        if(height < minHeight)
            minHeight = height
        if(height > maxHeight)
            maxHeight = height
    }

    fun detectedHeightRange(minHeight: Float, maxHeight: Float) {
        if(minHeight < this.minHeight)
            this.minHeight = minHeight
        if(maxHeight > this.maxHeight)
            this.maxHeight = maxHeight
    }

    fun detectedColor(color: ColorId) {
        if (colors.contains(color))
            return
        colors += color
    }

    fun onSensorDetectedHeight(height: Float) {
        detectedHeightRange(height - HEIGHT_TOLERANCE, height + HEIGHT_TOLERANCE)
    }

    fun onSensorDetectedColor(color: ColorId) {
        detectedColor(color)
    }

    fun getPossibleObstacles(): Array<Obstacle> {
        var possibleObstacles = arrayOf<Obstacle>()

        fun AddPossibleObstacle(obstacle: Obstacle) {
            possibleObstacles += obstacle
        }

        if (isFence())
            AddPossibleObstacle(Obstacle.FENCE)
        if (isTree())
            AddPossibleObstacle(Obstacle.TREE)
        if (isAnimal())
            AddPossibleObstacle(Obstacle.ANIMAL)
        if (isMyFeed())
            AddPossibleObstacle(Obstacle.FEED)
        if (isOpponentFeed())
            AddPossibleObstacle(Obstacle.FEED_OPPONENT)
        if (isMyStable())
            AddPossibleObstacle(Obstacle.STABLE)
        if (isOpponentStable())
            AddPossibleObstacle(Obstacle.STABLE_OPPONENT)
        if (isRobot())
            AddPossibleObstacle(Obstacle.ROBOT)
        return possibleObstacles
    }

    fun isObstacle(obstacle: Obstacle, unambiguous: Boolean = false): Boolean {
        if(unambiguous)
        {
            val possibleOstacles = getPossibleObstacles()
            return possibleOstacles.size == 1 && possibleOstacles[0] == obstacle
        }

        return when(obstacle) {
            Obstacle.FENCE -> isFence()
            Obstacle.TREE -> isTree()
            Obstacle.ANIMAL -> isAnimal()
            Obstacle.FEED -> isMyFeed()
            Obstacle.FEED_OPPONENT -> isOpponentFeed()
            Obstacle.STABLE -> isMyStable()
            Obstacle.STABLE_OPPONENT -> isOpponentStable()
            Obstacle.ROBOT -> isRobot()
            else -> {
                throw Error("only defined obstacles supported")
            }
        }
    }

    fun isTree(): Boolean {
        return colors.contains(Constants.ObstacleCheck.TREE_COLOR)
            && isHeightInRange(Constants.ObstacleCheck.TREE_HEIGHT)
    }

    fun isAnimal(): Boolean {
        return colors.any {
            !Constants.ObstacleCheck.NOT_ANIMAL_COLORS.contains(it)
        }
    }

    fun isMyFeed(): Boolean {
        return colors.contains(Config.myFeedColor) &&
                Constants.ObstacleCheck.FEED_HEIGHT in heightRange
    }

    fun isOpponentFeed(): Boolean {
        return colors.contains(Config.opponentFeedColor) &&
                Constants.ObstacleCheck.FEED_HEIGHT in heightRange
    }

    fun isMyStable(): Boolean {
        return colors.contains(Config.stableColor) &&
                Constants.StableDetection.STABLE_HEIGHT in heightRange
    }

    fun isOpponentStable(): Boolean {
        return colors.contains(Config.opponentStableColor) &&
                Constants.StableDetection.STABLE_HEIGHT in heightRange
    }

    fun isFence(): Boolean {
        return Constants.ObstacleCheck.FENCE_HEIGHT in heightRange
            //TODO: filter impossible colors
    }

    fun isRobot(): Boolean {
        return maxHeight > Constants.ObstacleCheck.ROBOT_DETECTION_MIN_HEIGHT
    }

    private fun isHeightInRange(range: ClosedFloatingPointRange<Float>): Boolean {
        return (minHeight in range) || (maxHeight in range)
    }

    companion object {
        const val HEIGHT_TOLERANCE = 0.5f //cm
    }
}