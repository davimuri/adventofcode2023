package com.dmmapps

import kotlin.math.min

class RangeMapEntry(destination: Long, source: Long, length: Long) {
    private val sourceStart = source
    private val sourceEnd = source + length - 1
    private val destinationStart = destination

    fun contains(value: Long) = value in sourceStart..sourceEnd

    fun get(sourceValue: Long) = destinationStart + (sourceValue - sourceStart)
}

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day5_input.txt")?.bufferedReader()?.readLines()
    val seedsPart1 = lines!![0].substring(7).split(" ").map { it.toLong() }.toList()
    val maps = parseMapLines(lines)
    val minLocation = seedsPart1.minOfOrNull { getLocation(it, maps) }
    println("min location part 1: $minLocation")

    var minLocationPart2 = Long.MAX_VALUE
    for (i in seedsPart1.indices step 2) {
        for (s in seedsPart1[i]..<seedsPart1[i] + seedsPart1[i+1]) {
            minLocationPart2 = min(minLocationPart2, getLocation(s, maps))
        }
    }
    println("min location part 2: $minLocationPart2")

}

fun parseMapLines(lines: List<String>): List<List<RangeMapEntry>> {
    val maps = mutableListOf<MutableList<RangeMapEntry>>()
    var currentMap = mutableListOf<RangeMapEntry>()
    maps.add(currentMap)
    for (i in 3..<lines.size) {
        val line = lines[i]
        if (line.isEmpty()) {
            continue
        }
        if (line.contains("map")) {
            currentMap = mutableListOf()
            maps.add(currentMap)
            continue
        }
        val tokens = line.split(" ")
        currentMap.add(RangeMapEntry(tokens[0].toLong(), tokens[1].toLong(), tokens[2].toLong()))
    }
    return maps
}

fun getLocation(seed: Long, maps: List<List<RangeMapEntry>>): Long {
    var location = seed
    for (map in maps) {
        for (range in map) {
            if (range.contains(location)) {
                location = range.get(location)
                break
            }
        }
    }
    return location
}