package com.dmmapps

import kotlin.random.Random

// https://www.scaler.com/topics/data-structures/kargers-algorithm-for-minimum-cut/

private data class Edge(val vertex1: Int, val vertex2: Int)

private data class MinCutResult(val edges: List<Edge>, val sets: List<Set<Int>>)

private class DisjointSet(val size: Int) {
    private val parent = IntArray(size) { i -> i }
    private val rank = IntArray(size) { 0 }

    fun findParent(node: Int): Int {
        if (node == parent[node]) {
            return node
        }
        parent[node] = findParent(parent[node])
        return parent[node]
    }

    fun join(node1: Int, node2: Int) {
        var parent1 = findParent(node1)
        var parent2 = findParent(node2)
        if (parent1 == parent2) {
            return
        }
        if (rank[parent2] < rank[parent1]) {
            val tmp = parent1
            parent1 = parent2
            parent2 = tmp
        }
        parent[parent1] = parent2
        if (rank[parent1] == rank[parent2]) {
            rank[parent2]++
        }
    }

    fun getSets(): List<Set<Int>> {
        val parentChildren = mutableMapOf<Int, MutableSet<Int>>()
        for (i in parent.indices) {
            val parent = findParent(i)
            val set = parentChildren.getOrPut(parent) { mutableSetOf<Int>() }
            set.add(i)
        }
        return parentChildren.values.toList()
    }
}

fun main() {
//    val lines = listOf(
//        "jqt: rhn xhk nvd",
//        "rsh: frs pzl lsr",
//        "xhk: hfx",
//        "cmg: qnr nvd lhk bvb",
//        "rhn: xhk bvb hfx",
//        "bvb: xhk hfx",
//        "pzl: lsr hfx nvd",
//        "qnr: nvd",
//        "ntq: jqt hfx bvb xhk",
//        "nvd: lhk",
//        "lsr: lhk",
//        "rzs: qnr cmg lsr rsh",
//        "frs: qnr lhk lsr"
//    )
    val lines = object {}.javaClass.getResourceAsStream("/day25_input.txt")?.bufferedReader()?.readLines()!!
    val vertexes = mutableMapOf<String, Int>()
    var vertexCount = 0
    val edges = mutableListOf<Edge>()
    lines.forEach { l ->
        val tokens = l.split(" ")
        val origin = tokens[0].substring(0, tokens[0].length-1)
        val originId = vertexes.getOrPut(origin) { vertexCount++ }
        for (i in 1..<tokens.size) {
            val vertex2Id = vertexes.getOrPut(tokens[i]) { vertexCount++ }
            edges.add(Edge(originId, vertex2Id))
        }
    }
    val vertexNumberToName = Array(vertexCount) { "" }
    vertexes.forEach { (name, number) -> vertexNumberToName[number] = name }

    println("Vertexes: $vertexes")
    println("Vertexes size: ${vertexes.size}")
    println("Edges size: ${edges.size}")
    var minCutResult = minCutKragerAlgorithm(vertexes, edges)
    var runs = 1
    while (minCutResult.edges.size > 3) {
        minCutResult = minCutKragerAlgorithm(vertexes, edges)
        runs++
    }
    println("After $runs runs...")
    println("Edges to cut:")
    for (edge in minCutResult.edges) {
        println("${vertexNumberToName[edge.vertex1]} -- ${vertexNumberToName[edge.vertex2]}")
    }
    val set1 = minCutResult.sets[0].map { vertexNumberToName[it] }
    val set2 = minCutResult.sets[1].map { vertexNumberToName[it] }
    println("Set 1 (${set1.size}): $set1")
    println("Set 2 (${set2.size}): $set2")
    println("Result ${set1.size * set2.size}")
}

private fun minCutKragerAlgorithm(vertexes: Map<String, Int>, edges: List<Edge>): MinCutResult {
    var vertexCount = vertexes.size
    val disjointSet = DisjointSet(vertexes.size)
    while (vertexCount > 2) {
        val currentEdge = edges[Random.nextInt(0, edges.size-1)]
        val parent1 = disjointSet.findParent(currentEdge.vertex1)
        val parent2 = disjointSet.findParent(currentEdge.vertex2)
        if (parent1 == parent2) {
            continue
        }
        disjointSet.join(parent1, parent2)
        vertexCount--
    }
    val edgesToCut = mutableListOf<Edge>()
    for (edge in edges) {
        if (disjointSet.findParent(edge.vertex1) != disjointSet.findParent(edge.vertex2)) {
            edgesToCut.add(edge)
        }
    }
    val sets = disjointSet.getSets()
    return MinCutResult(edgesToCut, sets)
}
