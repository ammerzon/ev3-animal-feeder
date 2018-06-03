package com.angrynerds.ev3.core

import com.angrynerds.ev3.enums.Obstacle
import com.angrynerds.ev3.util.Constants
import lejos.sensors.ColorId

class ObstacleInfo {
    var colorsVertical = emptyArray<ColorId>()
    var colorsForward = emptyArray<ColorId>()

    var minHeight: Float = 0f
    var maxHeight: Float = Float.POSITIVE_INFINITY
    val heightRange: ClosedFloatingPointRange<Float>
        get() = minHeight..maxHeight

    var possibleObstacles = arrayOf<Obstacle>()

    private fun detectedHeight(height: Float) {
        if (height < minHeight)
            minHeight = height
        if (height > maxHeight)
            maxHeight = height

        updatePossibleObstacles()
    }

    private fun detectedHeightRange(minHeight: Float, maxHeight: Float, extend: Boolean = false) {
        if (!extend || minHeight < this.minHeight)
            this.minHeight = minHeight
        if (!extend || maxHeight > this.maxHeight)
            this.maxHeight = maxHeight

        updatePossibleObstacles()
    }

    fun onSensorDetectedHeight(height: Float, extend: Boolean = false) {
        detectedHeightRange(height - HEIGHT_TOLERANCE, height + HEIGHT_TOLERANCE, extend)
    }

    fun onSensorDetectedColorVertical(color: ColorId) {
        detectedColorVertical(color)
    }

    fun onSensorDetectedColorForward(color: ColorId) {
        detectedColorHorizontal(color)
    }

    private fun detectedColorVertical(color: ColorId, extend: Boolean = false) {
        if (!extend) {
            colorsVertical = arrayOf(color)
            return
        }

        if (colorsVertical.contains(color))
            return
        colorsVertical += color

        updatePossibleObstacles()
    }

    private fun detectedColorHorizontal(color: ColorId, extend: Boolean = false) {
        if (!extend) {
            colorsForward = arrayOf(color)
            return
        }

        if (colorsForward.contains(color))
            return
        colorsForward += color

        updatePossibleObstacles()
    }

    fun anyObstaclePossible(): Boolean {
        return !possibleObstacles.isEmpty()
    }

    private fun updatePossibleObstacles() {
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

        this.possibleObstacles = possibleObstacles
    }

    fun isObstacle(obstacle: Obstacle, unambiguous: Boolean = false): Boolean {
        if (unambiguous) {
            return possibleObstacles.size == 1 && possibleObstacles[0] == obstacle
        }

        return possibleObstacles.contains(obstacle)
    }

    fun areObstacles(vararg obstacles: Obstacle, unambiguous: Boolean = false): Boolean {
        return (!unambiguous || obstacles.size == possibleObstacles.size)
                && obstacles.all { possibleObstacles.contains(it) }
    }

    private fun isTree(): Boolean {
        return isColorForwardPossible(Constants.ObstacleCheck.TREE_COLOR)   // TODO color sensor forward or vertical?
                && isHeightInRange(Constants.ObstacleCheck.TREE_HEIGHT)
    }

    private fun isAnimal(): Boolean {
        return (colorsForward.isEmpty() || colorsForward.any {
            !Constants.ObstacleCheck.NOT_ANIMAL_COLORS.contains(it)
        }) && isHeightInRange(Constants.ObstacleCheck.ANIMAL_HEIGHT)
    }

    private fun isMyFeed(): Boolean {
        return isColorForwardPossible(FeederRobot.feedColor)
        // && Constants.ObstacleCheck.FEED_HEIGHT in heightRange
    }

    private fun isOpponentFeed(): Boolean {
        return isColorForwardPossible(FeederRobot.opponentFeedColor)
        // && Constants.ObstacleCheck.FEED_HEIGHT in heightRange
    }

    private fun isMyStable(): Boolean {
        return isColorVerticalPossible(FeederRobot.stableColor) &&
                Constants.StableDetection.STABLE_HEIGHT in heightRange
    }

    private fun isOpponentStable(): Boolean {
        return isColorVerticalPossible(FeederRobot.opponentStableColor) &&
                Constants.StableDetection.STABLE_HEIGHT in heightRange
    }

    private fun isFence(): Boolean {
        return Constants.ObstacleCheck.FENCE_HEIGHT in heightRange
        //TODO: filter impossible colors
    }

    private fun isRobot(): Boolean {
        return maxHeight > Constants.ObstacleCheck.ROBOT_DETECTION_MIN_HEIGHT
    }

    fun isHeightInRange(range: ClosedFloatingPointRange<Float>): Boolean {
        return (minHeight in range) || (maxHeight in range)
    }

    fun isColorForwardPossible(color: ColorId): Boolean {
        return colorsForward.isEmpty() || colorsForward.contains(color)
    }

    fun isColorVerticalPossible(color: ColorId): Boolean {
        return colorsVertical.isEmpty() || colorsVertical.contains(color)
    }

    companion object {
        const val HEIGHT_TOLERANCE = 0.5f //cm
    }
}