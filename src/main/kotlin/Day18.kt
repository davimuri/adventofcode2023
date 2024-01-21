package com.dmmapps

import kotlin.math.abs

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day18_input.txt")?.bufferedReader()?.readLines()!!

    val instructionsPart1 = lines.map { parseInstruction(it, true) }
    val perimeterPart1 = instructionsPart1.sumOf { it.second }
    val points = getPoints(lines, true)
    val internalArea = shoelaceDay18(points)
    // from Pick's theorem:
    // A = i + b/2 - 1
    // A - b/2 + 1 = i
    // A - b/2 + 1 + b = i + b
    // A + b/2 + 1 = i + b
    // A: area using shoelace formula
    // b: perimeter
    // i + b: the total area whe need
    val totalAreaPart1 = internalArea + perimeterPart1/2 + 1
    println("Part 1 - total area: $totalAreaPart1, internal area $internalArea, border $perimeterPart1")

    val instructionsPart2 = lines.map { parseInstruction(it, false) }
    val perimeterPart2 = instructionsPart2.sumOf { it.second }
    val pointsPart2 = getPoints(lines, false)
    val internalAreaPart2 = shoelaceDay18(pointsPart2)
    val totalAreaPart2 = internalAreaPart2 + perimeterPart2/2 + 1
    println("Part 2 - total area: $totalAreaPart2, internal area $internalAreaPart2, border $perimeterPart2")
}

fun getPoints(inputLines: List<String>, part1: Boolean): List<Pair<Long, Long>> {
    var x = 0L
    var y = 0L
    val points = mutableListOf<Pair<Long, Long>>()
    for (line in inputLines) {
        val instruction = parseInstruction(line, part1)
        x += when (instruction.first) {
            'R' -> instruction.second
            'L' -> -instruction.second
            else -> 0
        }
        y += when (instruction.first) {
            'D' -> -instruction.second
            'U' -> instruction.second
            else -> 0
        }
        points.add(Pair(x, y))
    }
    return points
}

@OptIn(ExperimentalStdlibApi::class)
fun parseInstruction(line: String, part1: Boolean): Pair<Char, Long> {
    if (part1) {
        val tokens = line.split(" ")
        val meters = Integer.valueOf(tokens[1]).toLong()
        return Pair(tokens[0][0], meters)
    }
    val startIndex = line.indexOf('#') + 1
    val endIndex = line.indexOf(')')
    val direction = when (line[endIndex-1]) {
        '0' -> 'R'
        '1' -> 'D'
        '2' -> 'L'
        '3' -> 'U'
        else -> throw IllegalArgumentException("Wrong hexadecimal digit for direction ${line[endIndex-1]}")
    }
    val meters = line.substring(startIndex, endIndex-1).hexToLong()
    return Pair(direction, meters)
}

fun shoelaceDay18(points: List<Pair<Long, Long>>): Long {
    var sum = 0L
    for (i in points.indices) {
        sum -= points[i].first * points[(i+1) % points.size].second
        sum += points[i].second * points[(i+1) % points.size].first
    }
    return abs(sum / 2)
}
