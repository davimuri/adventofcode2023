package com.dmmapps

import kotlin.collections.HashSet

fun main() {
    var sum = 0
    val patternNumber = "\\d+".toRegex()
    val lines = object {}.javaClass.getResourceAsStream("/day3_input.txt")?.bufferedReader()?.readLines()
    val gearNumbers = mutableMapOf<String, MutableSet<Int>>()
    lines?.forEachIndexed {index, line ->
        sum += processLine(line, patternNumber, lines, index, gearNumbers)
    }
    println("Sum: $sum")
    val gearSum = gearNumbers.values.filter { it.size == 2 } .map {
        it.stream().reduce { a, b -> a * b }
    }.stream().map { it.get() } .reduce { a, b -> a + b }.get()
    println("Gear sum: $gearSum")
}

fun processLine(inputLine: String, p: Regex, lines: List<String>, lineIndex: Int, gearNumbers: MutableMap<String, MutableSet<Int>>): Int{
    var sum = 0
    p.findAll(inputLine).forEach { match ->
        var adjacentSymbol = false
        val value = match.value.toInt()
        val colLeft = match.range.first-1
        if (colLeft >= 0) {
            adjacentSymbol = adjacentSymbol || isSymbol(inputLine[colLeft])
            addGearNumbers(lines, lineIndex, colLeft, value, gearNumbers)
            if (lineIndex-1 >= 0) {
                adjacentSymbol = adjacentSymbol || isSymbol(lines[lineIndex-1][colLeft])
                addGearNumbers(lines, lineIndex-1, colLeft, value, gearNumbers)
            }
            if (lineIndex+1 < lines.size) {
                adjacentSymbol = adjacentSymbol || isSymbol(lines[lineIndex+1][colLeft])
                addGearNumbers(lines, lineIndex+1, colLeft, value, gearNumbers)
            }
        }
        val colRight = match.range.last+1
        if (colRight < inputLine.length) {
            adjacentSymbol = adjacentSymbol || isSymbol(inputLine[colRight])
            addGearNumbers(lines, lineIndex, colRight, value, gearNumbers)
            if (lineIndex-1 >= 0) {
                adjacentSymbol = adjacentSymbol || isSymbol(lines[lineIndex-1][colRight])
                addGearNumbers(lines, lineIndex-1, colRight, value, gearNumbers)
            }
            if (lineIndex+1 < lines.size) {
                adjacentSymbol = adjacentSymbol || isSymbol(lines[lineIndex+1][colRight])
                addGearNumbers(lines, lineIndex+1, colRight, value, gearNumbers)
            }
        }
        if (adjacentSymbol || match.range.any { thereIsSymbolUpOrDown(lines, lineIndex, it) }) {
            sum += value
        }
        if (lineIndex-1 >= 0) {
            match.range.forEach { col ->
                addGearNumbers(lines, lineIndex - 1, col, value, gearNumbers)
            }
        }
        if (lineIndex+1 < lines.size) {
            match.range.forEach { col ->
                addGearNumbers(lines, lineIndex + 1, col, value, gearNumbers)
            }
        }
    }
    return sum
}

fun addGearNumbers(lines: List<String>, row: Int, col: Int, value: Int, gearNumbers: MutableMap<String, MutableSet<Int>>) {
    if (lines[row][col] == '*') {
        val key = "$row - $col"
        val numbers = gearNumbers[key] ?: HashSet()
        numbers.add(value)
        gearNumbers[key] = numbers
    }
}

fun thereIsSymbolUpOrDown(lines: List<String>, lineIndex: Int, index: Int): Boolean{
    val charUp = if (lineIndex-1 < 0) '.' else lines[lineIndex-1][index]
    val charDown = if (lineIndex+1 >= lines.size) '.' else lines[lineIndex+1][index]
    return isSymbol(charUp) || isSymbol(charDown)
}

fun isSymbol(letter: Char) = letter != '.' && !letter.isDigit()
