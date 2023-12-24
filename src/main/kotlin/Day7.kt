package com.dmmapps

enum class HandType {
    HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_OF_A_KIND, FULL_HOUSE, FOUR_OF_A_KIND, FIVE_OF_A_KIND
}

open class Hand(private val cards: String, val bid: Int): Comparable<Hand> {

    open val type: HandType by lazy {
        val cardCount = Array(13) {0}
        cards.forEach {
            val index = cardValue(it)
            cardCount[index]++
        }
        val maxCount = cardCount.maxOrNull()
        val groupCount = cardCount.count { it > 0 }
        when (maxCount) {
            5 -> HandType.FIVE_OF_A_KIND
            4 -> HandType.FOUR_OF_A_KIND
            3 -> when (groupCount) {
                2 -> HandType.FULL_HOUSE
                3 -> HandType.THREE_OF_A_KIND
                else -> throw IllegalArgumentException("wrong group count $groupCount for maxCount 3")
            }
            2 -> when (groupCount) {
                3 -> HandType.TWO_PAIR
                4 -> HandType.ONE_PAIR
                else -> throw IllegalArgumentException("wrong group $groupCount count for maxCount 2")
            }
            1 -> HandType.HIGH_CARD
            else -> throw IllegalArgumentException("wrong maxCount $maxCount")
        }
    }

    open fun cardValue(card: Char) = when (card) {
        '2' -> 0
        '3' -> 1
        '4' -> 2
        '5' -> 3
        '6' -> 4
        '7' -> 5
        '8' -> 6
        '9' -> 7
        'T' -> 8
        'J' -> 9
        'Q' -> 10
        'K' -> 11
        'A' -> 12
        else -> throw IllegalArgumentException("Invalid card symbol")
    }

    // the weakest hand goes first
    override fun compareTo(other: Hand): Int {
        val typeDiff = type.ordinal - other.type.ordinal
        if (typeDiff != 0) {
            return typeDiff
        }
        for (i in cards.indices) {
            val diff = cardValue(cards[i]) - cardValue(other.cards[i])
            if (diff != 0) {
                return diff
            }
        }
        throw IllegalArgumentException("Two hands are equal: $cards")
    }

    override fun toString() = cards
}

class HandWithJoker(cards: String, bid: Int) : Hand(cards, bid) {
    override val type: HandType by lazy {
        val cardCount = Array(13) {0}
        cards.forEach {
            val index = cardValue(it)
            cardCount[index]++
        }
        val maxCount = cardCount.maxOrNull()
        val groupCount = cardCount.count { it > 0 }
        val jokerCount = cardCount[cardValue('J')]
        when (maxCount) {
            5 -> HandType.FIVE_OF_A_KIND
            4 -> when  {
                jokerCount > 0 -> HandType.FIVE_OF_A_KIND
                else -> HandType.FOUR_OF_A_KIND
            }
            3 -> when (groupCount) {
                2 -> when {
                    jokerCount > 0 -> HandType.FIVE_OF_A_KIND
                    else -> HandType.FULL_HOUSE
                }
                3 -> when (jokerCount) {
                    3 -> HandType.FOUR_OF_A_KIND
                    1 -> HandType.FOUR_OF_A_KIND
                    else -> HandType.THREE_OF_A_KIND
                }
                else -> throw IllegalArgumentException("wrong group count $groupCount for maxCount 3")
            }
            2 -> when (groupCount) {
                3 -> when (jokerCount) {
                    2 -> HandType.FOUR_OF_A_KIND
                    1 -> HandType.FULL_HOUSE
                    else -> HandType.TWO_PAIR
                }
                4 -> when (jokerCount) {
                    2 -> HandType.THREE_OF_A_KIND
                    1 -> HandType.THREE_OF_A_KIND
                    else -> HandType.ONE_PAIR
                }
                else -> throw IllegalArgumentException("wrong group $groupCount count for maxCount 2")
            }
            1 -> when (jokerCount) {
                1 -> HandType.ONE_PAIR
                else -> HandType.HIGH_CARD
            }
            else -> throw IllegalArgumentException("wrong maxCount $maxCount")
        }
    }

    override fun cardValue(card: Char) = when (card) {
        'J' -> 0
        '2' -> 1
        '3' -> 2
        '4' -> 3
        '5' -> 4
        '6' -> 5
        '7' -> 6
        '8' -> 7
        '9' -> 8
        'T' -> 9
        'Q' -> 10
        'K' -> 11
        'A' -> 12
        else -> throw IllegalArgumentException("Invalid card symbol")
    }

}

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day7_input.txt")?.bufferedReader()?.readLines()
    val hands = lines!!.map {
        val tokens = it.split(" ")
        Hand(tokens[0], tokens[1].toInt())
    }.toList().sorted()
    val resPart1 = hands.mapIndexed { i, h ->
        (i+1) * h.bid
    }.sum()
    println("Result part 1: $resPart1")

    val handsWithJoker = lines.map {
        val tokens = it.split(" ")
        HandWithJoker(tokens[0], tokens[1].toInt())
    }.toList().sorted()
    val resPart2 = handsWithJoker.mapIndexed { i, h ->
        (i+1) * h.bid
    }.sum()
    println("Result part 2: $resPart2")

}