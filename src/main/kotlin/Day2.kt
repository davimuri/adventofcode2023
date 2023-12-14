package com.dmmapps

import java.util.regex.Pattern
import kotlin.math.max

const val RED = 12
const val GREEN = 13
const val BLUE = 14

fun main() {
    var sum = 0
    var powerSum = 0
    val patternRed = Pattern.compile("(\\d+) red")
    val patternGreen = Pattern.compile("(\\d+) green")
    val patternBlue = Pattern.compile("(\\d+) blue")
    val patternGameId = Pattern.compile("Game (\\d+):")
    object {}.javaClass.getResourceAsStream("/day2_input.txt")?.bufferedReader()?.forEachLine {
        if (possible(it, patternRed, RED) && possible(it, patternGreen, GREEN) && possible(it, patternBlue, BLUE)) {
            sum += getGameId(it, patternGameId)
        }
        powerSum += getMax(it, patternRed) * getMax(it, patternGreen) * getMax(it, patternBlue)
    }
    println("Sum: $sum")
    println("Sum of powers: $powerSum")
}

fun possible(inputLine: String, p: Pattern, max: Int): Boolean {
    val m = p.matcher(inputLine)
    while (m.find()) {
        if (m.group(1).toInt() > max) {
            return false
        }
    }
    return true
}

fun getGameId(inputLine: String, p: Pattern): Int {
    val m = p.matcher(inputLine)
    if (m.find()) {
        return m.group(1).toInt()
    }
    throw IllegalArgumentException("Input line doesn't match")
}

fun getMax(inputLine: String, p: Pattern): Int {
    val m = p.matcher(inputLine)
    var maxValue = 0
    while (m.find()) {
        maxValue = max(m.group(1).toInt(), maxValue)
    }
    return maxValue
}
