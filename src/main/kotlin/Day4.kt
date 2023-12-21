package com.dmmapps

import kotlin.math.pow

fun main() {
    var sum = 0
    val lines = object {}.javaClass.getResourceAsStream("/day4_input.txt")?.bufferedReader()?.readLines()
    val cardPoints = Array(lines!!.size) {0}
    lines.forEach {
        val cardNum = getCardNumber(it)
        val matches = getMatches(it)
        sum += calculatePointsPart1(matches)
        cardPoints[cardNum-1] = matches
    }
    val cardsAmount = calculateCardsPart2(cardPoints)
    println("Sum: $sum")
    println("Cards: $cardsAmount")
}

fun calculatePointsPart1(matches: Int): Int {
    if (matches == 0) {
        return 0
    }
    return 2.toFloat().pow(matches-1).toInt()
}

fun calculateCardsPart2(cardPoints: Array<Int>): Int {
    val cardInstances = Array(cardPoints.size) {1}
    cardPoints.forEachIndexed { cardNum, points ->
        val currentAmount = cardInstances[cardNum]
        for (i in cardNum+1..cardNum+points) {
            cardInstances[i] += currentAmount
        }
    }
    return cardInstances.sum()
}

fun getMatches(line: String): Int {
    val winingNumbers = getWiningNumbers(line.substring(10, 40).trim())
    winingNumbers.remove("")
    val numbers = line.substring(42).trim().split(" ").toCollection(mutableSetOf())
    numbers.remove("")
    return numbers.intersect(winingNumbers).size
}

fun getWiningNumbers(str: String) =
    str.split(" ").toCollection(mutableSetOf())


fun getCardNumber(line: String) = line.substring(5, 8).trim().toInt()
