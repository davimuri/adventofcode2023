package com.dmmapps

import java.util.LinkedList
import kotlin.math.max
import kotlin.math.min

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day21_input.txt")?.bufferedReader()?.readLines()!!
    val matrix = Array(lines.size) { i -> lines[i].toCharArray() }
    val start = findStart(matrix)
    val plotsPart1 = countPlotsReached(matrix, start, 64)
    println("Part 1 - Plots reached: $plotsPart1")
    val plotsPart2 = countPlotsPart2(matrix, start)
    println("Part 1 - Plots reached: $plotsPart2")
}

// Solution from https://www.youtube.com/watch?v=9UOMZSL0JTg
fun countPlotsPart2(matrix: Array<CharArray>, start: Pair<Int, Int>): Long {
    val gridWidth = 26_501_365L / matrix.size - 1

    val odd = (gridWidth / 2 * 2 + 1) * (gridWidth / 2 * 2 + 1)
    val even = ((gridWidth + 1) / 2 * 2) * ((gridWidth + 1) / 2 * 2)

    val oddPoints = countPlotsReached(matrix, start, matrix.size * 2 + 1)
    val evenPoints = countPlotsReached(matrix, start, matrix.size * 2)

    val cornerTop = countPlotsReached(matrix, Pair(start.first, matrix.size - 1), matrix.size - 1)
    val cornerRight = countPlotsReached(matrix, Pair(0, start.second), matrix.size - 1)
    val cornerBottom = countPlotsReached(matrix, Pair(start.first, 0),matrix.size - 1)
    val cornerLeft = countPlotsReached(matrix, Pair(matrix.size-1, start.second), matrix.size - 1)

    val smallTopRight = countPlotsReached(matrix, Pair(0, matrix.size-1), matrix.size / 2 - 1)
    val smallTopLeft = countPlotsReached(matrix, Pair(matrix.size-1, matrix.size-1), matrix.size / 2 - 1)
    val smallBottomRight = countPlotsReached(matrix, Pair(0, 0), matrix.size / 2 - 1)
    val smallBottomLeft = countPlotsReached(matrix, Pair(matrix.size-1, 0), matrix.size / 2 - 1)

    val largeTopRight = countPlotsReached(matrix, Pair(0, matrix.size-1), matrix.size * 3 / 2 - 1)
    val largeTopLeft = countPlotsReached(matrix, Pair(matrix.size-1, matrix.size-1), matrix.size * 3 / 2 - 1)
    val largeBottomRight = countPlotsReached(matrix, Pair(0, 0), matrix.size * 3 / 2 - 1)
    val largeBottomLeft = countPlotsReached(matrix, Pair(matrix.size-1, 0), matrix.size * 3 / 2 - 1)

    return odd * oddPoints +
            even * evenPoints +
            cornerTop + cornerRight + cornerBottom + cornerLeft +
            (gridWidth + 1) * (smallTopRight + smallTopLeft + smallBottomRight + smallBottomLeft) +
            gridWidth * (largeTopRight + largeTopLeft + largeBottomRight + largeBottomLeft)
}

fun countPlotsReached(matrix: Array<CharArray>, start: Pair<Int, Int>, steps: Int): Int {
    val queue = LinkedList<Pair<Int, Int>>()
    queue.add(start)
    var notesToRead = 1
    var currentStep = 0
    while (currentStep < steps) {
        currentStep++
        val notesToAdd = mutableSetOf<Pair<Int, Int>>()
        for (i in 1..notesToRead) {
            val pos = queue.removeFirst()
            for (dir in DIRECTIONS) {
                val nextPos = Pair(pos.first+dir.first, pos.second+dir.second)
                if (nextPos.second in matrix.indices && nextPos.first in matrix[0].indices
                    && !notesToAdd.contains(nextPos) && matrix[nextPos.second][nextPos.first] != '#') {
                    notesToAdd.add(nextPos)
                }
            }
        }
        notesToRead = notesToAdd.size
        queue.addAll(notesToAdd)
    }
    return notesToRead
}

fun countPlotsReachedInfiniteMatrix(matrix: Array<CharArray>, start: Pair<Int, Int>): Int {
    val queue = LinkedList<Pair<Int, Int>>()
    queue.add(start)
    var notesToRead = 1
    var steps = 0
    var minRow = start.second
    var maxRow = start.second
    var minCol = start.first
    var maxCol = start.first
    while (steps < 26_501_365) {
        steps++
        val notesToAdd = mutableSetOf<Pair<Int, Int>>()
        for (i in 1..notesToRead) {
            val pos = queue.removeFirst()
            for (dir in DIRECTIONS) {
                val nextPos = Pair(pos.first+dir.first, pos.second+dir.second)
                minRow = min(minRow, nextPos.second)
                maxRow = max(maxRow, nextPos.second)
                minCol = min(minCol, nextPos.first)
                maxCol = max(maxCol, nextPos.first)
                val convertedPos = convertPosition(nextPos, matrix.size, matrix[0].size)
                if (!notesToAdd.contains(nextPos) && matrix[convertedPos.second][convertedPos.first] != '#') {
                    notesToAdd.add(nextPos)
                }
            }
        }
        notesToRead = notesToAdd.size
        queue.addAll(notesToAdd)
    }
    println("steps $steps, minRow $minRow, minCol $minCol, maxRow $maxRow, maxCol $maxCol")
    return notesToRead
}

private fun findStart(matrix: Array<CharArray>): Pair<Int, Int> {
    for (i in matrix.indices) {
        for (j in matrix[i].indices) {
            if (matrix[i][j] == 'S') {
                return Pair(j, i)
            }
        }
    }
    return Pair(0, 0)
}

private fun convertPosition(pos: Pair<Int, Int>, rows: Int, cols: Int): Pair<Int, Int> {
    var x = pos.first % cols
    if (x < 0) {
        x += cols
    }
    var y = pos.second % rows
    if (y < 0) {
        y += rows
    }
    return Pair(x, y)
}

