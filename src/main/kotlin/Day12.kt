package com.dmmapps

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day12_input.txt")?.bufferedReader()?.readLines()
    var sumOfCountsPart1 = 0L
    var sumOfCountsPart2 = 0L
    lines!!.forEach {
        val tokens = it.split(" ")
        val groups = tokens[1].split(",").map { groupSize -> groupSize.toInt() }.toIntArray()
        var cache = mutableMapOf<String, Long>()
        sumOfCountsPart1 += countCombinationsNew(tokens[0], 0, groups, 0, 0, cache)
        cache = mutableMapOf()
        sumOfCountsPart2 += countCombinationsNew(unfoldRecord(tokens[0], 5), 0, unfoldGroups(groups, 5), 0, 0, cache)
    }
    println("Sum part 1: $sumOfCountsPart1")
    println("Sum part 2: $sumOfCountsPart2")
}

fun countCombinationsNew(record: String, start: Int, groups: IntArray, groupIndex: Int, currentGroupLength: Int, cache: MutableMap<String, Long>): Long {
    val key = "$start - $groupIndex - $currentGroupLength"
    if (cache.containsKey(key)) {
        return cache[key]!!
    }
    if (start == record.length) {
        if (groupIndex == groups.size-1 && currentGroupLength == groups[groupIndex]) {
            return 1
        }
        if (groupIndex == groups.size && currentGroupLength == 0) {
            return 1
        }
        return 0
    }
    var count = 0L
    if (record[start] == '.') {
        if (groupIndex < groups.size && currentGroupLength == groups[groupIndex]) {
            count += countCombinationsNew(record, start+1, groups, groupIndex+1, 0, cache)
        } else if (currentGroupLength == 0) {
            count += countCombinationsNew(record, start+1, groups, groupIndex, 0, cache)
        }
    } else if (record[start] == '#') {
        if (groupIndex < groups.size && currentGroupLength < groups[groupIndex]) {
            count += countCombinationsNew(record, start + 1, groups, groupIndex, currentGroupLength + 1, cache)
        }
    } else if (record[start] == '?') {
        if (currentGroupLength == 0) {
            count += countCombinationsNew(record, start+1, groups, groupIndex, 0, cache)
        }
        if (groupIndex < groups.size) {
            if (currentGroupLength == groups[groupIndex]) {
                count += countCombinationsNew(record, start + 1, groups, groupIndex + 1, 0, cache)
            } else if (currentGroupLength < groups[groupIndex]) {
                count += countCombinationsNew(record, start + 1, groups, groupIndex, currentGroupLength + 1, cache)
            }
        }
    }
    cache[key] = count
    return count
}

fun unfoldRecord(record: String, times: Int): String {
    val sb = StringBuilder(record.length*times + times-1)
    for (i in 1..<times) {
        sb.append(record)
        sb.append("?")
    }
    sb.append(record)
    return sb.toString()
}

fun unfoldGroups(groups: IntArray, times: Int): IntArray {
    val unfolded = IntArray(groups.size*times)
    for (i in 0..<times) {
        System.arraycopy(groups, 0, unfolded, i*groups.size, groups.size)
    }
    return unfolded
}
