package com.dmmapps

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day8_input.txt")?.bufferedReader()?.readLines()
    val leftNetwork = mutableMapOf<String, String>()
    val rightNetwork = mutableMapOf<String, String>()
    for (i in 2..<lines!!.size) {
        parseNetworkEntry(lines[i], leftNetwork, rightNetwork)
    }
    val steps = countSteps(lines[0], "AAA", true, leftNetwork, rightNetwork)
    println("Steps part 1: $steps") // 16897

    val startingNodesPart2 = leftNetwork.keys.filter { it[2] == 'A' }
    println("Starting nodes part 2: $startingNodesPart2")
    val endNodesPart2 = leftNetwork.keys.filter { it[2] == 'Z' }
    println("Ending nodes part 2: $endNodesPart2")
    val stepsPerNode = Array(startingNodesPart2.size) {0}
    for (i in startingNodesPart2.indices) {
        stepsPerNode[i] = countSteps(lines[0], startingNodesPart2[i], false, leftNetwork, rightNetwork)
    }
    println("steps per node: ${stepsPerNode.toList()}")

    val stepsPart2 = findStepsWhenAllEnd(stepsPerNode)
    println("Steps part 2: $stepsPart2")
}

fun parseNetworkEntry(line: String, leftNetwork: MutableMap<String, String>, rightNetwork: MutableMap<String, String>) {
    val source = line.substring(0, 3)
    val left = line.substring(7, 10)
    val right = line.substring(12, 15)
    leftNetwork[source] = left
    rightNetwork[source] = right
}

fun countSteps(path: String, start: String, allZ: Boolean,
                        leftNetwork: Map<String, String>, rightNetwork: Map<String, String>): Int {
    var steps = 0
    var currentNode = start
    var index = 0
    while (!isEnd(currentNode, allZ)) {
        currentNode = if (path[index] == 'R') {
            rightNetwork[currentNode]!!
        } else {
            leftNetwork[currentNode]!!
        }
        steps++
        index = (index + 1) % path.length
    }
    return steps
}

fun isEnd(node: String, allZ: Boolean) = (allZ && node == "ZZZ") || (!allZ && node[2] == 'Z')

fun countSteps(path: String, startingNodes: List<String>,
               leftNetwork: Map<String, String>, rightNetwork: Map<String, String>): Long {
    var steps = 0L
    val currentNodes = startingNodes.toTypedArray()
    var index = 0
    var isEnd = false
    while (!isEnd) {
        if (path[index] == 'R') {
            for (i in currentNodes.indices) {
                currentNodes[i] = rightNetwork[currentNodes[i]]!!
            }
        } else {
            for (i in currentNodes.indices) {
                currentNodes[i] = leftNetwork[currentNodes[i]]!!
            }
        }
        steps++
        index = (index + 1) % path.length
        isEnd = currentNodes.all { it[2] == 'Z' }
        if (currentNodes.count { it[2] == 'Z' } >= 4) {
            println("steps: $steps, current nodes: ${currentNodes.toList()}")

        }
    }
    return steps
}

fun findEndingNodes(path: String, startNode: String,
                    leftNetwork: Map<String, String>, rightNetwork: Map<String, String>): Set<String> {
    var currentNode = startNode
    var index = 0
    var isEnd = false
    val endNodes = mutableSetOf<String>()
    var steps = 0
    while (!isEnd) {
        currentNode = if (path[index] == 'R') {
            rightNetwork[currentNode]!!
        } else {
            leftNetwork[currentNode]!!
        }
        steps++
        index = (index + 1) % path.length
        if (isEnd(currentNode, false) && !endNodes.contains(currentNode)) {
            endNodes.add(currentNode)
            println("added $currentNode, steps: $steps")
        } else if (isEnd(currentNode, false) && endNodes.contains(currentNode)) {
            isEnd = true
            println("visited $currentNode, steps: $steps")
        }
    }
    return endNodes
}

fun findStepsWhenAllEnd(stepsPerNode: Array<Int>): Long {
    val minSteps = stepsPerNode.min()
    var cycles = 1
    var end = false
    while (!end) {
        val min = minSteps.toLong() * cycles
        end = stepsPerNode.all { min % it == 0L }
        if (end) {
            println("end with min $min and minSteps $minSteps")
        }
        cycles++
    }
    return minSteps * (cycles - 1L)
}