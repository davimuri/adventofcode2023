package com.dmmapps

import kotlin.math.max

val DOWN = Pair(0, 1)
val UP = Pair(0, -1)
val RIGHT = Pair(1, 0)
val LEFT = Pair(-1, 0)
val DIRECTION_MAP = mapOf(
    Pair(Pair('/', RIGHT), listOf(UP)),
    Pair(Pair('/', LEFT), listOf(DOWN)),
    Pair(Pair('/', DOWN), listOf(LEFT)),
    Pair(Pair('/', UP), listOf(RIGHT)),

    Pair(Pair('\\', RIGHT), listOf(DOWN)),
    Pair(Pair('\\', LEFT), listOf(UP)),
    Pair(Pair('\\', DOWN), listOf(RIGHT)),
    Pair(Pair('\\', UP), listOf(LEFT)),

    Pair(Pair('|', RIGHT), listOf(UP, DOWN)),
    Pair(Pair('|', LEFT), listOf(UP, DOWN)),
    Pair(Pair('|', DOWN), listOf(DOWN)),
    Pair(Pair('|', UP), listOf(UP)),

    Pair(Pair('-', RIGHT), listOf(RIGHT)),
    Pair(Pair('-', LEFT), listOf(LEFT)),
    Pair(Pair('-', DOWN), listOf(LEFT, RIGHT)),
    Pair(Pair('-', UP), listOf(LEFT, RIGHT)),
)

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day16_input.txt")?.bufferedReader()?.readLines()!!
    val contraption = Array(lines.size) { i -> lines[i].toCharArray() }
    println("contraption size: ${contraption.size}, ${contraption[0].size}")
    val visited = Array(lines.size) { i -> Array(lines[i].length) { mutableSetOf<Pair<Int, Int>>() } }
    followLight(0,0, RIGHT, contraption, visited)
    val tilesEnergized = sumTilesEnergized(visited)
    println("Tiles energized: $tilesEnergized") // 7860
    val maxTiles = maximizeEnergizedTiles(contraption)
    println("part 2: $maxTiles")
}

fun maximizeEnergizedTiles(contraption: Array<CharArray>): Int {
    var maxValue = 0
    for (i in contraption[0].indices) {
        var visited = Array(contraption.size) { j -> Array(contraption[j].size) { mutableSetOf<Pair<Int, Int>>() } }
        followLight(0, i, DOWN, contraption, visited)
        maxValue = max(maxValue, sumTilesEnergized(visited))

        visited = Array(contraption.size) { j -> Array(contraption[j].size) { mutableSetOf<Pair<Int, Int>>() } }
        followLight(contraption.size-1, i, UP, contraption, visited)
        maxValue = max(maxValue, sumTilesEnergized(visited))

    }
    for (i in contraption.indices) {
        var visited = Array(contraption.size) { j -> Array(contraption[j].size) { mutableSetOf<Pair<Int, Int>>() } }
        followLight(i, 0, RIGHT, contraption, visited)
        maxValue = max(maxValue, sumTilesEnergized(visited))

        visited = Array(contraption.size) { j -> Array(contraption[j].size) { mutableSetOf<Pair<Int, Int>>() } }
        followLight(i, contraption[i].size-1, LEFT, contraption, visited)
        maxValue = max(maxValue, sumTilesEnergized(visited))

    }
    return maxValue
}

fun followLight(row: Int, col: Int, dir: Pair<Int, Int>, contraption: Array<CharArray>, visited: Array<Array<MutableSet<Pair<Int, Int>>>>) {
    if (row < 0 || row >= contraption.size || col < 0 || col >= contraption[0].size || visited[row][col].contains(dir)) {
        return
    }
    visited[row][col].add(dir)
    if (contraption[row][col] == '.') {
        followLight(row+dir.second, col+dir.first, dir, contraption, visited)
    } else {
        DIRECTION_MAP[Pair(contraption[row][col], dir)]!!.forEach { nextDir ->
            followLight(row+nextDir.second, col+nextDir.first, nextDir, contraption, visited)
        }
    }
}

fun sumTilesEnergized(visited: Array<Array<MutableSet<Pair<Int, Int>>>>) = visited.sumOf { row -> row.count { it.isNotEmpty() } }

fun printBeams(contraption: Array<CharArray>, visited: Array<Array<MutableSet<Pair<Int, Int>>>>) {
    visited.forEachIndexed { i, row ->
        row.forEachIndexed {j, set ->
            if (contraption[i][j] == '.') {
                val symbol = when (set.size) {
                    0 -> contraption[i][j]
                    1 -> when (set.first()) {
                        UP -> '^'
                        DOWN -> 'v'
                        RIGHT -> '>'
                        LEFT -> '<'
                        else -> throw IllegalStateException("Wrong direction ${set.first()}")
                    }
                    else -> set.size
                }
                print(symbol)
            } else {
                print(contraption[i][j])
            }
        }
        println()
    }
}
