package com.dmmapps

val SPELLED_DIGITS = arrayOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")

fun main() {
    val res = calculate("one6two")
    println("Res: $res")
    var sum = 0
    object {}.javaClass.getResourceAsStream("/day1_input.txt")?.bufferedReader()?.forEachLine {
        sum += calculate(it)
    }
    println("Sum: $sum")
}

fun calculate(str: String): Int {
    var digit1 = -1
    var digit2 = -1
    str.forEachIndexed {i, letter ->
        val currentDigit = when {
            letter in '0'..'9' -> letter.digitToInt()
            else -> getDigitFromSpelledOutNumber(str, i)
        }
        if (currentDigit != -1) {
            if (digit1 == -1) {
                digit1 = currentDigit
            } else {
                digit2 = currentDigit
            }
        }
    }
    var sum = 0
    if (digit1 == -1) {
        return 0
    }
    sum = digit1 * 10
    if (digit2 == -1) {
        sum += digit1
    } else {
        sum += digit2
    }
    return sum
}

fun getDigitFromSpelledOutNumber(str: String, index: Int): Int {
    SPELLED_DIGITS.forEachIndexed {i, digit ->
        if (isSpelledOut(str, index, digit)) {
            return i+1
        }
    }
    return -1
}

fun isSpelledOut(str: String, index: Int, spelled: String): Boolean {
    if (index + spelled.length > str.length) {
        return false
    }
    for (i in 0 until spelled.length) {
        if (str[index+i] != spelled[i]) {
            return false
        }
    }
    return true
}