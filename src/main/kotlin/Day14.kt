package com.dmmapps

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day14_input.txt")?.bufferedReader()?.readLines()!!
    println("input size: (${lines.size}, ${lines[0].length})")
    val sumPart1 = solvePart1(lines)
    println("Sum part 1: $sumPart1")
    val matrix = Array(lines.size) { i -> lines[i].toCharArray() }
    val sumPart2 = solvePart2(matrix)
    println("Sum part 2: $sumPart2")
}

fun solvePart1(platform: List<String>): Long {
    val counter = LongArray(platform.size)
    for (col in platform[0].indices) {
        var rocks = 0
        var startRow = 0
        for (row in platform.indices) {
            when (platform[row][col]) {
                '#' -> {
                    increment(counter, startRow, startRow + rocks)
                    startRow = row + 1
                    rocks = 0
                }
                'O' -> rocks++
            }
        }
        increment(counter, startRow, startRow + rocks)
    }
    var length = counter.size
    return counter.sumOf { e -> e * length-- }
}

fun solvePart2(matrix: Array<CharArray>): Long {
    val matrixList = mutableListOf<Array<CharArray>>()
    matrixList.add(cloneMatrix(matrix))
    var foundMatrixIndex = -1
    var cycle = 0
    val totalCycles = 1_000_000_000
    while (foundMatrixIndex == -1) {
        for (i in 1..4) {
            tiltNorth(matrix)
            rotate90Clockwise(matrix)
        }
        cycle++
        foundMatrixIndex = indexOf(matrix, matrixList)
        if (foundMatrixIndex >= 0) {
            println("matrix found at $foundMatrixIndex at cycle $cycle")
        } else {
            matrixList.add(cloneMatrix(matrix))
        }
        if (cycle % 1_000_000 == 0) {
            println(cycle)
        }
    }
    if (foundMatrixIndex >= 0) {
        val remainingCycles = (totalCycles - cycle) % (cycle - foundMatrixIndex)
        val savedCycles = totalCycles - cycle - remainingCycles
        println("initials cycles executed: $cycle, remaining cycles to execute: $remainingCycles, " +
                "total executed: ${cycle+remainingCycles}, saved: $savedCycles")
        for (j in 1..remainingCycles) {
            for (i in 1..4) {
                tiltNorth(matrix)
                rotate90Clockwise(matrix)
            }
        }
    }
    matrix.forEach {
            row -> row.forEach {
        print("$it")
    }
        println()
    }
    var lengthFromSouth = matrix.size
    var sum = 0L
    matrix.forEach {row ->
        val count = row.count { it == 'O' }
        sum += count * lengthFromSouth
        lengthFromSouth--
    }
    return sum
}

fun tiltNorth(matrix: Array<CharArray>) {
    for (col in matrix[0].indices) {
        var rocks = 0
        var startRow = 0
        for (row in matrix.indices) {
            when (matrix[row][col]) {
                '#' -> {
                    setRocks(matrix, col, startRow, row, rocks)
                    startRow = row + 1
                    rocks = 0
                }
                'O' -> rocks++
            }
        }
        setRocks(matrix, col, startRow, matrix.size, rocks)
    }
}

fun increment(counter: LongArray, start: Int, end: Int) {
    for (i in start..<end) {
        counter[i]++
    }
}

fun setRocks(matrix: Array<CharArray>, col: Int, startRow: Int, endRow: Int, rocks: Int) {
    for (i in startRow..<startRow+rocks) {
        matrix[i][col] = 'O'
    }
    for (i in startRow+rocks..<endRow) {
        matrix[i][col] = '.'
    }
}

fun rotate90Clockwise(matrix: Array<CharArray>) {
    transpose(matrix)
    reverseRows(matrix)
}

fun transpose(matrix:Array<CharArray>) {
    for (i in matrix.indices) {
        for (j in i+1..<matrix[i].size) {
            val temp = matrix[j][i]
            matrix[j][i] = matrix[i][j]
            matrix[i][j] = temp
        }
    }
}

fun reverseRows(matrix: Array<CharArray>) {
    for (i in matrix.indices) {
        var left = 0
        var right = matrix[i].size - 1
        while (left < right) {
            val temp = matrix[i][left]
            matrix[i][left] = matrix[i][right]
            matrix[i][right] = temp
            left++
            right--
        }
    }
}

fun cloneMatrix(matrix: Array<CharArray>): Array<CharArray> {
    val cloned = Array(matrix.size) { CharArray(matrix[0].size) }
    for (i in matrix.indices) {
        for (j in matrix[i].indices) {
            cloned[i][j] = matrix[i][j]
        }
    }
    return cloned
}

fun indexOf(matrix: Array<CharArray>, matrixList: List<Array<CharArray>>): Int {
    for (i in matrixList.size-1 downTo 0) {
        if (areEqual(matrix, matrixList[i])) {
            return i
        }
    }
    return -1
}

fun areEqual(matrix1: Array<CharArray>, matrix2: Array<CharArray>): Boolean {
    for (i in matrix1.indices) {
        for (j in matrix1[i].indices) {
            if (matrix1[i][j] != matrix2[i][j]) {
                return false
            }
        }
    }
    return true
}
