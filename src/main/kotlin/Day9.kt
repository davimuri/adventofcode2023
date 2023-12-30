package com.dmmapps

fun main() {
    var sumRight = 0L
    var sumLeft = 0L
    object {}.javaClass.getResourceAsStream("/day9_input.txt")?.bufferedReader()?.forEachLine {
        val inputLine = it.split(" ").map { e -> e.toLong() }
        val res = naiveSolution(inputLine)
        sumLeft += res[0]
        sumRight += res[1]
    }
    println("Solution part 1: $sumRight")
    println("Solution part 2: $sumLeft")
}

fun naiveSolution(inputLine: List<Long>): Array<Long> {
    val calculatedLines = mutableListOf<List<Long>>()
    calculatedLines.add(inputLine)
    var currentLine = inputLine
    var allZeros = inputLine.all { it == 0L }
    while (!allZeros) {
        val newLine = mutableListOf<Long>()
        calculatedLines.add(newLine)
        for (i in 0..<currentLine.size - 1) {
            newLine.add(currentLine[i+1] - currentLine[i])
        }
        allZeros = newLine.all { it == 0L }
        currentLine = newLine
    }
    var predictionRight = 0L
    var predictionLeft = 0L
    for (i in calculatedLines.size-2 downTo 0) {
        predictionRight += calculatedLines[i].last()
        predictionLeft = calculatedLines[i].first() - predictionLeft
    }
    return arrayOf(predictionLeft, predictionRight)
}
