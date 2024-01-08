package com.dmmapps

import kotlin.math.abs

val PIPE_CONNECTIONS = mapOf(
    Pair('|', listOf(Pair(0, 1), Pair(0, -1))),
    Pair('-', listOf(Pair(1, 0), Pair(-1, 0))),
    Pair('L', listOf(Pair(0, -1), Pair(1, 0))),
    Pair('J', listOf(Pair(0, -1), Pair(-1, 0))),
    Pair('7', listOf(Pair(0, 1), Pair(-1, 0))),
    Pair('F', listOf(Pair(0, 1), Pair(1, 0))),
    Pair('S', emptyList()),
    Pair('.', emptyList()),
)

val DIRECTIONS = arrayOf(Pair(-1, 0), Pair(0, 1), Pair(1, 0), Pair(0, -1))

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day10_input.txt")?.bufferedReader()?.readLines()
    var row = 0
    var col = 0
    for (i in lines!!.indices) {
        col = lines[i].indexOf('S')
        if (col >= 0) {
            row = i
            break
        }
    }
    val pathNodes = move(Pair(col, row), lines)
    println("Steps part 1: ${pathNodes.size / 2}")
    val pathNodesSet = pathNodes.toSet()
    printPath(pathNodesSet, lines)
    println()
    calculateEnclosedTiles(pathNodes)
}

fun move(start: Pair<Int, Int>, map: List<String>): List<Pair<Int, Int>> {
    var currentPos = DIRECTIONS
        .map { getTarget(start, it) }
        .first {target ->
            !outOfBounds(target, map.size, map[0].length)
                    && sourcePipeConnectsWithTarget(map[target.second][target.first], target, start)
        }
    var prevPos = start
    var steps = 1
    val pathList = mutableListOf<Pair<Int, Int>>()
    val pathNodes = mutableSetOf<Pair<Int, Int>>()
    pathNodes.add(start)
    pathList.add(start)
    var reachedStartAgain = false
    while (!reachedStartAgain) {
        pathNodes.add(currentPos)
        pathList.add(currentPos)
        val currentPipe = map[currentPos.second][currentPos.first]
        for (dir: Pair<Int, Int> in PIPE_CONNECTIONS[currentPipe]!!) {
            val nextPos = getTarget(currentPos, dir)
            if (outOfBounds(nextPos, map.size, map[0].length)) {
                continue
            }
            val nextPipe = map[nextPos.second][nextPos.first]
            if (nextPipe == 'S' || (nextPos != prevPos && sourcePipeConnectsWithTarget(nextPipe, nextPos, currentPos))) {
                prevPos = currentPos
                currentPos = nextPos
                steps++
                break
            }
        }
        reachedStartAgain = currentPipe == 'S'
    }
    return pathList
}

fun sourcePipeConnectsWithTarget(pipe: Char, source: Pair<Int, Int>, target: Pair<Int, Int>): Boolean {
    if (!PIPE_CONNECTIONS.containsKey(pipe)) {
        println("pipe not found $pipe")
    }
    return PIPE_CONNECTIONS[pipe]!!.any { dir ->
        val current = getTarget(source, dir)
        current == target
    }
}

fun getTarget(source: Pair<Int, Int>, direction: Pair<Int, Int>) =
    Pair(source.first + direction.first, source.second + direction.second)


fun outOfBounds(pos: Pair<Int, Int>, rows: Int,  cols: Int) =
    pos.first < 0 || pos.first >= cols || pos.second < 0 || pos.second >= rows

fun printPath(pathNodes: Set<Pair<Int, Int>>, map: List<String>) {
    for (i in map.indices) {
        for (j in 0..<map[0].length) {
            if (pathNodes.contains(Pair(j, i))) {
                print(map[i][j])
            } else {
                print(".")
            }
        }
        println()
    }
}

// https://www.reddit.com/r/adventofcode/comments/18f1sgh/2023_day_10_part_2_advise_on_part_2/
fun calculateEnclosedTiles(path: List<Pair<Int, Int>>): Int {
    val area = shoelace(path)
    val tiles = picksTheorem(area, path.size)
    println("area: $area , enclosed tiles: $tiles")
    return tiles
}

// https://en.wikipedia.org/wiki/Shoelace_formula
fun shoelace(points: List<Pair<Int, Int>>): Int {
    var sum = 0
    for (i in points.indices) {
        val yi = points[i].second
        val xiMinus1 = when {
            i-1 < 0 -> points.last().first
            else -> points[i-1].first
        }
        val xiPlus1 = when {
            i+1 >= points.size -> points.first().first
            else -> points[i+1].first
        }
        sum += yi * (xiMinus1 - xiPlus1)
    }
    return abs(sum / 2)
}

// https://en.wikipedia.org/wiki/Pick%27s_theorem
fun picksTheorem(area: Int, boundary: Int): Int {
    // A = i + b/2 - 1
    // A - b/2 + 1 = i
    return area - boundary/2 + 1
}