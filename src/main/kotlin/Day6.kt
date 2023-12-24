package com.dmmapps

// V * T = D
// X * (T-X) > D, 0 < X < T

fun main() {
    val times = arrayOf(62L, 73, 75, 65)
    val distances = arrayOf(644L, 1023, 1240, 1023)
    var resPart1 = 1
    for (i in times.indices) {
        resPart1 *= findRange(times[i], distances[i]).count()
    }
    println("Result part 1: $resPart1")

    val resPart2 = findRange(62737565, 644102312401023).count()
    println("Result part 2: $resPart2")
}

fun findRange(time: Long, distance: Long): LongRange {
    var minVelocity = 1L
    for (i in 1..<time) {
        val currentDistance = i * (time - i)
        if (currentDistance > distance) {
            minVelocity = i
            break
        }
    }
    var maxVelocity = time - 1
    for (i in maxVelocity downTo 1) {
        val currentDistance = i * (time - i)
        if (currentDistance > distance) {
            maxVelocity = i
            break
        }
    }
    return minVelocity..maxVelocity
}