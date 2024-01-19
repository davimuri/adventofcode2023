package com.dmmapps

import java.util.LinkedList

private data class NodeBFS(
    var row: Int,
    var col: Int,
    var heatLoss: Int,
    var direction: Pair<Int, Int>,
    var straightSteps: Int,
    var previous: NodeBFS? = null
)

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day17_input.txt")?.bufferedReader()?.readLines()!!
    val city = Array(lines.size) {i ->
        lines[i].toCharArray().map { Integer.valueOf(it.toString()) }.toIntArray()
    }
    val heatLossBFS = bfs(city, 1, 3)
    println("Min heat loss Part 1: $heatLossBFS")
    val heatLossPart2 = bfs(city, 4, 10)
    println("Min heat loss Part 2: $heatLossPart2")

}

fun bfs(city: Array<IntArray>, minSteps: Int, maxSteps: Int): Int {
    val queue = LinkedList<NodeBFS>()
    queue.add(NodeBFS(0, 0, 0, RIGHT, 1))
    queue.add(NodeBFS(0, 0, 0, DOWN, 1))
    val visited = mutableMapOf<String, Int>()
    var minHeatLoss = Integer.MAX_VALUE
    var lastNode = NodeBFS(0, 0, 0, Pair(0, 0), 0)
    while (queue.isNotEmpty()) {
        val node = queue.poll()
        visited[Triple(node.col, node.row, node.direction.toString()).toString()] = node.heatLoss
        if (node.row == city.size-1 && node.col == city.last().size-1) {
            if (minHeatLoss > node.heatLoss) {
                minHeatLoss = node.heatLoss
                lastNode = node
                println("found min $minHeatLoss - queue size ${queue.size}")
            }
            continue
        }
        val rotated = Pair(node.direction.second, node.direction.first)
        val rotatedNeg = Pair(-1*rotated.first, -1*rotated.second)
        addNodesBFS(node, rotated, minSteps, maxSteps, queue, visited, city)
        addNodesBFS(node, rotatedNeg, minSteps, maxSteps, queue, visited, city)
    }
    var pathNode: NodeBFS? = lastNode
    val pathList = mutableListOf<Pair<Int, Int>>()
    while (pathNode != null) {
        pathList.add(Pair(pathNode.col, pathNode.row))
        pathNode = pathNode.previous
    }
    println(pathList)
    city.forEachIndexed { i, row ->
        row.forEachIndexed { j, value ->
            val pathIndex = pathList.indexOf(Pair(j, i))
            if (pathIndex >= 0) {
                print("*")
            }
            else {
                print(value)
            }
        }
        println()
    }
    return minHeatLoss
}

private fun addNodesBFS(previous: NodeBFS, direction: Pair<Int, Int>,
                        minSteps: Int, maxSteps: Int,
                        queue: LinkedList<NodeBFS>, visited: MutableMap<String, Int>, city: Array<IntArray>) {
    var tmpPrevNode = previous
    var heatLoss = previous.heatLoss
    var row = previous.row
    var col = previous.col
    for (step in 1..<minSteps) {
        row += direction.second
        col += direction.first
        if (row < 0 || row >= city.size || col < 0 || col >= city[0].size) {
            return
        }
        heatLoss += city[row][col]
        tmpPrevNode = NodeBFS(row, col, heatLoss, direction, step, tmpPrevNode)
    }
    for (step in minSteps..maxSteps) {
        row += direction.second
        col += direction.first
        if (row < 0 || row >= city.size || col < 0 || col >= city[0].size) {
            return
        }
        val visitedKey = Triple(col, row, direction.toString()).toString()
        heatLoss += city[row][col]
        tmpPrevNode = NodeBFS(row, col, heatLoss, direction, step, tmpPrevNode)
        if (!visited.containsKey(visitedKey) || visited[visitedKey]!! > heatLoss) {
            queue.add(tmpPrevNode)
            visited[visitedKey] = heatLoss
        }
    }
}
