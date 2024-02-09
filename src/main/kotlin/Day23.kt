package com.dmmapps

import java.util.LinkedList
import kotlin.math.max

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day23_input.txt")?.bufferedReader()?.readLines()!!
    val matrix = Array(lines.size) { i ->
        lines[i].toCharArray()
    }
    val start = Pair(matrix.first().indexOf('.'), 0)
    val end = Pair(matrix.last().indexOf('.'), matrix.size-1)
    val graph = createGraph(matrix, start, end, true)
    val maxDistance = dfs(graph, start, end, mutableSetOf())
    println("Part 1 - max steps: $maxDistance")
    val graph2 = createGraph(matrix, start, end, false)
    val maxDistance2 = dfs(graph2, start, end, mutableSetOf())
    println("Part 2 - max steps: $maxDistance2")
}

fun createGraph(matrix: Array<CharArray>, start: Pair<Int, Int>,
                end: Pair<Int, Int>, slipperySlopes: Boolean): Map<Pair<Int, Int>, Map<Pair<Int, Int>, Int>> {
    val newVertexes = mutableSetOf<Pair<Int, Int>>()
    newVertexes.add(start)
    for (i in matrix.indices) {
        for (j in matrix[i].indices) {
            if (matrix[i][j] != '#') {
                var neighbors = 0
                DIRECTIONS.forEach { dir ->
                    val nextRow = i + dir.second
                    val nextCol = j + dir.first
                    if (inbounds(nextRow, nextCol, matrix) && matrix[nextRow][nextCol] != '#') {
                        neighbors++
                    }
                }
                if (neighbors >= 3) {
                    newVertexes.add(Pair(j, i))
                }
            }
        }
    }
    newVertexes.add(end)
    val graph = buildMap {
        newVertexes.forEach { v -> put(v, mutableMapOf<Pair<Int, Int>, Int>()) }
    }
    val directions = if (slipperySlopes) mapOf(
        Pair('.', DIRECTIONS),
        Pair('>', arrayOf(Pair(1, 0))),
        Pair('<', arrayOf(Pair(-1, 0))),
        Pair('^', arrayOf(Pair(0, -1))),
        Pair('v', arrayOf(Pair(0, 1))),
    ) else mapOf(
        Pair('.', DIRECTIONS),
        Pair('>', DIRECTIONS),
        Pair('<', DIRECTIONS),
        Pair('^', DIRECTIONS),
        Pair('v', DIRECTIONS),
    )
    for (v in newVertexes) {
        val queue = LinkedList<Pair<Pair<Int, Int>, Int>>()
        queue.add(Pair(v, 0))
        val visited = mutableSetOf<Pair<Int, Int>>()
        visited.add(v)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val point = current.first
            val steps = current.second
            if (steps != 0 && point in newVertexes) {
                graph[v]!![point] = steps
                continue
            }
            for (dir in directions[matrix[point.second][point.first]]!!) {
                val nextRow = point.second + dir.second
                val nextCol = point.first + dir.first
                val nextPoint = Pair(nextCol, nextRow)
                if (inbounds(nextRow, nextCol, matrix)
                    && matrix[nextRow][nextCol] != '#' && !visited.contains(nextPoint)) {
                    queue.add(Pair(nextPoint, steps + 1))
                    visited.add(nextPoint)
                }
            }
        }
    }
    return graph
}

private fun dfs(graph: Map<Pair<Int, Int>, Map<Pair<Int, Int>, Int>>,
                pos: Pair<Int, Int>, end: Pair<Int, Int>, visited: MutableSet<Pair<Int, Int>>): Int {

    if (pos == end) {
        return 0
    }
    visited.add(pos)
    var maxDistance = Integer.MIN_VALUE
    graph[pos]!!.forEach { (nextPos, distance) ->
        if (!visited.contains(nextPos)) {
            maxDistance = max(maxDistance, distance + dfs(graph, nextPos, end, visited))
        }
    }
    visited.remove(pos)
    return maxDistance
}

fun inbounds(row: Int, col: Int, matrix: Array<CharArray>) = row in matrix.indices && col in matrix[row].indices
