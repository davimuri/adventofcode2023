package com.dmmapps

import java.util.Optional

fun main() {
    var inputMatrix = mutableListOf<CharArray>()
    var sumPart1 = 0L
    var sumPart2 = 0L
    var currentCase = 0
    object {}.javaClass.getResourceAsStream("/day13_input.txt")?.bufferedReader()?.forEachLine {line ->
        if (line.isEmpty() && inputMatrix.isNotEmpty()) {
            sumPart1 += solveCase(inputMatrix)
            sumPart2 += solveCaseBruteForcePart2(inputMatrix)
            currentCase++
            println("Current case: $currentCase")
            inputMatrix = mutableListOf()
        } else {
            inputMatrix.add(line.toCharArray())
        }
    }
    sumPart1 += solveCase(inputMatrix)
    sumPart2 += solveCaseBruteForcePart2(inputMatrix)
    println("Sum part 1: $sumPart1")
    println("Sum part 2: $sumPart2")
}

fun solveCase(matrix: List<CharArray>): Long {
    var colsLeftOfReflection = 0
    for (col in 0..matrix[0].size-2) {
        if (matrix[0][col] == matrix[0][col+1]) {
            var allSame = true
            for (row in 1..<matrix.size) {
                if (matrix[row][col] != matrix[row][col+1]) {
                    allSame = false
                }
            }
            if (allSame && fullVerticalReflection(matrix, col+1)) {
                colsLeftOfReflection = col+1
            }
        }
    }
    var rowsAboveOfReflection = 0
    for (row in 0..matrix.size-2) {
        if (matrix[row][0] == matrix[row+1][0]) {
            var allSame = true
            for (col in 1..<matrix[row].size) {
                if (matrix[row][col] != matrix[row+1][col]) {
                    allSame = false
                }
            }
            if (allSame && fullHorizontalReflection(matrix, row+1)) {
                rowsAboveOfReflection = row+1
            }
        }
    }
    return colsLeftOfReflection + 100L * rowsAboveOfReflection
}

fun fullVerticalReflection(matrix: List<CharArray>, rightCol: Int): Boolean {
    for (row in matrix.indices) {
        var left = rightCol - 1
        var right = rightCol
        while (left >= 0 && right < matrix[row].size) {
            if (matrix[row][left] != matrix[row][right]) {
                return false
            }
            left--
            right++
        }
    }
    return true
}

fun fullHorizontalReflection(matrix: List<CharArray>, bottomRow: Int): Boolean {
    for (col in matrix[0].indices) {
        var top = bottomRow - 1
        var bottom = bottomRow
        while (top >= 0 && bottom < matrix.size) {
            if (matrix[top][col] != matrix[bottom][col]) {
                return false
            }
            top--
            bottom++
        }
    }
    return true
}

fun solveCaseBruteForcePart2(matrix: List<CharArray>): Long {
    var colsLeftOfReflection = 0
    var rowsAboveOfReflection = 0

    for (row in 1..<matrix.size) {
        val smudge = horizontalReflectionPart2(matrix, row)
        if (smudge.isPresent) {
            rowsAboveOfReflection = row
            break
        }
    }
    if (rowsAboveOfReflection == 0) {
        for (col in 1..<matrix[0].size) {
            val smudge = verticalReflectionPart2(matrix, col)
            if (smudge.isPresent) {
                colsLeftOfReflection = col
                break
            }
        }
    }
    return colsLeftOfReflection + 100L * rowsAboveOfReflection
}

fun verticalReflectionPart2(matrix: List<CharArray>, rightCol: Int): Optional<Pair<Int, Int>> {
    var differences = 0
    var smudgeRow = -1
    var smudgeCol = -1
    for (row in matrix.indices) {
        var left = rightCol - 1
        var right = rightCol
        while (left >= 0 && right < matrix[row].size) {
            if (matrix[row][left] != matrix[row][right]) {
                differences++
                smudgeRow = row
                smudgeCol = right
            }
            if (differences > 1) {
                return Optional.empty()
            }
            left--
            right++
        }
    }
    if (differences == 1) {
        return Optional.of(Pair(smudgeCol, smudgeRow))
    }
    return Optional.empty()
}

fun horizontalReflectionPart2(matrix: List<CharArray>, bottomRow: Int): Optional<Pair<Int, Int>> {
    var differences = 0
    var smudgeRow = -1
    var smudgeCol = -1
    for (col in matrix[0].indices) {
        var top = bottomRow - 1
        var bottom = bottomRow
        while (top >= 0 && bottom < matrix.size) {
            if (matrix[top][col] != matrix[bottom][col]) {
                differences++
                smudgeRow = top
                smudgeCol = col
            }
            if (differences > 1) {
                return Optional.empty()
            }
            top--
            bottom++
        }
    }
    if (differences == 1) {
        return Optional.of(Pair(smudgeCol, smudgeRow))
    }
    return Optional.empty()
}
