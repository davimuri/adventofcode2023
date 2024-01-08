package com.dmmapps

import kotlin.math.abs

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day11_input.txt")?.bufferedReader()?.readLines()
    val galaxies = mutableListOf<Pair<Int, Int>>()
    val emptyRows = Array(lines!!.size) { true }
    val emptyCols = Array(lines[0].length) { true }
    for (i in lines.indices) {
        for (j in lines[i].indices) {
            if (lines[i][j] == '#') {
                galaxies.add(Pair(j, i))
                emptyRows[i] = false
                emptyCols[j] = false
            }
        }
    }
    var sumOfLengths = 0
    var sumOfLengthsPart2 = 0L
    for (i in galaxies.indices) {
        for (j in i+1..<galaxies.size) {
            val g1 = galaxies[i]
            val g2 = galaxies[j]
            val emptiesX = countEmpties(g1.first, g2.first, emptyCols)
            val emptiesY = countEmpties(g1.second, g2.second, emptyRows)
            val distX = abs(g2.first - g1.first)
            val distY = abs(g2.second - g1.second)
            sumOfLengths += distX + emptiesX + distY + emptiesY
            sumOfLengthsPart2 += distX + (emptiesX * 1_000_000L) - emptiesX +
                    distY + (emptiesY * 1_000_000L) - emptiesY
        }
    }
    println("Sum of lengths: $sumOfLengths")
    println("Sum of lengths part 2: $sumOfLengthsPart2")
}


fun countEmpties(start: Int, end: Int, empties: Array<Boolean>): Int {
    val range = when {
        end-start > 0 -> start..end
        else -> end..start
    }
    var count = 0
    for (i in range) {
        if (empties[i]) {
            count++
        }
    }
    return count
}
