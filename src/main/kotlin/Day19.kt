package com.dmmapps

import kotlin.math.max
import kotlin.math.min


private data class Part(val x: Long, val m: Long, val a: Long, val s: Long) {

    fun get(field: Char) = when (field) {
        'x' -> x
        'm' -> m
        'a' -> a
        's' -> s
        else -> throw IllegalArgumentException("Wrong field name $field")
    }

    fun sumRatings() = x + m + a + s
}

private data class Condition(val field: Char, val operator: String, val constant: Long, val destination: String) {

    fun matches(part: Part): Boolean {
        val partValue = part.get(field)
        return (operator == ">" && partValue > constant) ||
                (operator == "<" && partValue < constant) ||
                (operator == "=" && partValue == constant)
    }

    fun negate(): Condition {
        val opNegated = when (operator) {
            ">" -> "<="
            "<" -> ">="
            "=" -> "!="
            else -> throw IllegalArgumentException("Wrong operator $operator")
        }
        return Condition(field, opNegated, constant, destination)
    }
}

private data class Rule(val name: String, val conditions: List<Condition>, val lastDestination: String) {

    fun getDestination(part: Part): String {
        for (condition in conditions) {
            if (condition.matches(part)) {
                return condition.destination
            }
        }
        return lastDestination
    }
}

val PART_X = "x=(\\d+)".toRegex()
val PART_M = "m=(\\d+)".toRegex()
val PART_A = "a=(\\d+)".toRegex()
val PART_S = "s=(\\d+)".toRegex()

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day19_input.txt")?.bufferedReader()?.readLines()!!
    val rules = mutableMapOf<String, Rule>()
    val parts = mutableListOf<Part>()
    var readingRules = true
    for (l in lines) {
        if (l.isEmpty()) {
            readingRules = false
        } else {
            if (readingRules) {
                val rule = parseRule(l)
                rules[rule.name] = rule
            } else {
                parts.add(parsePart(l))
            }
        }
    }

    var sumPart1 = 0L
    for (p in parts) {
        if (evaluatePart(p, rules)) {
            sumPart1 += p.sumRatings()
        }
    }
    println("Sum part 1: $sumPart1")

    dfs(rules["in"]!!, 0, emptyList(), rules)
    val combinationsCount = countAcceptedCombinations()
    println("Part 2 - Combinations count: $combinationsCount")
}

private fun parsePart(line: String): Part {
    val x = PART_X.find(line)!!.groupValues[1].toLong()
    val m = PART_M.find(line)!!.groupValues[1].toLong()
    val a = PART_A.find(line)!!.groupValues[1].toLong()
    val s = PART_S.find(line)!!.groupValues[1].toLong()
    return Part(x, m, a, s)
}

private fun parseRule(line: String): Rule {
    val conditionsStart = line.indexOf('{')
    val ruleName = line.substring(0, conditionsStart)
    val conditions = line.substring(conditionsStart+1, line.length-1).split(",")
    val conditionList = mutableListOf<Condition>()
    for (i in 0..conditions.size-2) {
        val cond = conditions[i]
        val field = cond[0]
        val operator = cond[1].toString()
        val colonIndex = cond.indexOf(':')
        val constant = cond.substring(2, colonIndex).toLong()
        val destination = cond.substring(colonIndex+1)
        conditionList.add(Condition(field, operator, constant, destination))
    }
    val lastDestination = conditions.last()
    return Rule(ruleName, conditionList, lastDestination)
}

private fun evaluatePart(part: Part, rules: Map<String, Rule>): Boolean {
    var rule = rules["in"]!!
    while (true) {
        val destination = rule.getDestination(part)
        if (destination == "A") {
            return true
        }
        if (destination == "R") {
            return false
        }
        rule = rules[destination]!!
    }
}

private val acceptedConditions = mutableListOf<List<Condition>>()

private fun dfs(currentRule: Rule, currentConditionIndex: Int, conditionsVisited: List<Condition>, rules: Map<String, Rule>) {
    if (currentConditionIndex >= currentRule.conditions.size) {
        if (currentRule.lastDestination == "R") {
            return
        }
        if (currentRule.lastDestination == "A") {
            acceptedConditions.add(conditionsVisited.toList())
        } else {
            dfs(rules[currentRule.lastDestination]!!, 0, conditionsVisited, rules)
        }
        return
    }
    val currentCondition = currentRule.conditions[currentConditionIndex]
    if (currentCondition.destination == "A") {
        val newList = conditionsVisited.toMutableList()
        newList.add(currentCondition)
        acceptedConditions.add(newList)
    }  else if (currentCondition.destination != "R") {
        val newList = conditionsVisited.toMutableList()
        newList.add(currentCondition)
        dfs(rules[currentCondition.destination]!!, 0, newList, rules)
    }

    val newList = conditionsVisited.toMutableList()
    newList.add(currentCondition.negate())
    dfs(currentRule, currentConditionIndex+1, newList, rules)
}

private fun countAcceptedCombinations(): Long {
    val rangesList = mutableListOf<LongArray>()
    val nonEqualList = mutableListOf<Array<MutableList<Long>>>()
    for (conditions in acceptedConditions) {
        val res = calculateRanges(conditions)
        rangesList.add(res.first)
        nonEqualList.add(res.second)
    }
    var totalCount = 0L
    for (j in rangesList.indices) {
        val ranges = rangesList[j]
        val notEqualValues = nonEqualList[j]
        var currentCombinationCount = 1L
        for (i in 0..6 step 2) {
            var currentRangeCount = ranges[i + 1] - ranges[i] + 1
            for (e in notEqualValues[i / 2]) {
                println("non Equal value $e")
                if (ranges[i] <= e && e <= ranges[i + 1]) {
                    currentRangeCount--
                }
            }
            currentCombinationCount *= currentRangeCount
        }
        totalCount += currentCombinationCount
    }
    return totalCount
}

private fun calculateRanges(conditions: List<Condition>): Pair<LongArray, Array<MutableList<Long>>> {
    // x, m, a, s: each field has min and max possible value in the array
    val ranges = arrayOf(1L, 4000L, 1L, 4000L, 1L, 4000L, 1L, 4000L).toLongArray()
    val notEqualValues = arrayOf(mutableListOf<Long>(), mutableListOf(), mutableListOf(), mutableListOf())
    for (c in conditions) {
        val index = when (c.field) {
            'x' -> 0
            'm' -> 2
            'a' -> 4
            's' -> 6
            else -> throw IllegalArgumentException("Wrong field name in condition ${c.field}")
        }
        when (c.operator) {
            ">" -> ranges[index] = max(c.constant+1, ranges[index])
            ">=" -> ranges[index] = max(c.constant, ranges[index])
            "<" -> ranges[index+1] = min(c.constant-1, ranges[index+1])
            "<=" -> ranges[index+1] = min(c.constant, ranges[index+1])
            "=" -> {
                if (c.constant < ranges[index] || c.constant > ranges[index+1]){
                    throw IllegalArgumentException("The condition chain is not possible $conditions")
                }
                ranges[index] = c.constant
                ranges[index+1] = c.constant
            }
            "!=" -> {
                if (ranges[index] != 1L || ranges[index+1] != 4000L) {
                    throw IllegalStateException("The condition range is invalid for != operator")
                }
                notEqualValues[index/2].add(c.constant)
            }
        }
    }
    return Pair(ranges, notEqualValues)
}
