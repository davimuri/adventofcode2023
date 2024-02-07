package com.dmmapps

import java.util.LinkedList
import kotlin.math.max
import kotlin.math.min

private data class Brick(val x1: Int, val y1: Int, val z1: Int, val x2: Int, val y2: Int, val z2: Int)

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day22_input.txt")?.bufferedReader()?.readLines()!!
    var bricks = buildList {
        lines.forEach {
            add(parseInputLine(it))
        }
    }
    bricks = fallBricks(bricks.sortedBy { b -> b.z1 })

    val brickAsKeyIsSupportedByBricksInValues = buildMap(bricks.size) {
        bricks.forEach {
            put(it, mutableSetOf<Brick>())
        }
    }
    val brickAsKeySupportsBricksInValues = buildMap(bricks.size) {
        bricks.forEach {
            put(it, mutableSetOf<Brick>())
        }
    }
    for (i in bricks.size-1 downTo 0) {
        val current = bricks[i]
        var j = i - 1
        while (j >= 0 /*&& bricks[j].z1 > current.z1 - 2*/) {
            if (bricks[j].z2 + 1 == current.z1 && intersect(current, bricks[j])) {
                brickAsKeyIsSupportedByBricksInValues[current]!!.add(bricks[j])
                brickAsKeySupportsBricksInValues[bricks[j]]!!.add(current)
            }
            j--
        }
    }
    var countPart1 = 0
    for (brickDown in bricks) {
        val canRemove = brickAsKeySupportsBricksInValues[brickDown]!!.all { brickUp ->
            brickAsKeyIsSupportedByBricksInValues[brickUp]!!.size >= 2
        }
        if (canRemove) {
            countPart1++
        }
    }
    println("Part 1 - bricks can be removed: $countPart1")

    var totalFalling = 0
    for (brickDown in bricks) {
        val queue = LinkedList<Brick>()
        queue.addAll(
            brickAsKeySupportsBricksInValues[brickDown]!!.filter { brickUp ->
                brickAsKeyIsSupportedByBricksInValues[brickUp]!!.size == 1
            }
        )
        val falling = mutableSetOf<Brick>()
        falling.addAll(queue)
        var fallingBricks = 0
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            fallingBricks++
            val candidates = brickAsKeySupportsBricksInValues[current]!!.filter { brickUp ->
                !falling.contains(brickUp) && falling.containsAll(brickAsKeyIsSupportedByBricksInValues[brickUp]!!)
            }
            queue.addAll(candidates)
            falling.addAll(candidates)
        }
        totalFalling += falling.size
    }
    println("Part 2 - Falling bricks $totalFalling")


}

private fun parseInputLine(line: String): Brick {
    val points = line.split('~')
    val p1 = points[0].split(",")
    val p2 = points[1].split(",")
    return Brick(p1[0].toInt(), p1[1].toInt(), p1[2].toInt(), p2[0].toInt(), p2[1].toInt(), p2[2].toInt())
}

private fun fallBricks(bricks: List<Brick>): List<Brick> {
    val newOrder = mutableListOf<Brick>()
    bricks.forEachIndexed { i, current ->
        var newZ1 = 1
        for (j in i-1 downTo 0) {
            if (intersect(current, newOrder[j])) {
                newZ1 = max(newZ1, newOrder[j].z2 + 1)
            }
        }
        newOrder.add(Brick(current.x1, current.y1, newZ1, current.x2, current.y2, current.z2-current.z1+newZ1))
    }
    return newOrder.sortedBy { b -> b.z1 }
}

private fun intersect(brick1: Brick, brick2: Brick): Boolean =
    max(brick1.x1, brick2.x1) <= min(brick1.x2, brick2.x2) && max(brick1.y1, brick2.y1) <= min(brick1.y2, brick2.y2)
