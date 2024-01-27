package com.dmmapps

import java.util.LinkedList
import java.util.Objects
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private enum class Pulse { LOW, HIGH }

private data class Message(val source: Module, val destination: Module, val pulse: Pulse)

private abstract class Module(val name: String) {
    val inputs = mutableListOf<Module>()
    protected val outputs = mutableListOf<Module>()
    abstract fun accept(pulse: Pulse, module: Module): List<Message>

    open fun addInput(module: Module) {
        inputs.add(module)
    }

    fun addOutput(module: Module) {
        outputs.add(module)
        module.addInput(this)
    }

    fun send(pulse: Pulse): List<Message> {
        return outputs.map { m ->
            Message(this, m, pulse)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is Module) {
            return name == other.name
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        val outputNames = outputs.map { m -> m.name }.toString()
        return "$name -> $outputNames"
    }
}

private class FlipFlop(name: String) : Module(name) {
    private var on: Boolean = false
    override fun accept(pulse: Pulse, module: Module): List<Message> {
        if (pulse == Pulse.HIGH) {
            return emptyList()
        }
        if (on) {
            on = false
            return send(Pulse.LOW)
        }
        on = true
        return send(Pulse.HIGH)
    }

    override fun hashCode(): Int {
        return "$name - $on".hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as FlipFlop

        return on == other.on
    }
}

private class Conjunction(name: String) : Module(name) {
    val memory = mutableListOf<Pulse>()

    override fun accept(pulse: Pulse, module: Module): List<Message> {
        val index = inputs.indexOf(module)
        memory[index] = pulse
        if (memory.any { it == Pulse.LOW }) {
            return send(Pulse.HIGH)
        }
        return send(Pulse.LOW)
    }

    override fun addInput(module: Module) {
        memory.add(Pulse.LOW)
        super.addInput(module)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, memory)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Conjunction

        return memory == other.memory
    }
}

private class Output(name: String) : Module(name) {
    override fun accept(pulse: Pulse, module: Module): List<Message> {
        return emptyList()
    }
}

private class Broadcaster : Module("broadcaster") {
    override fun accept(pulse: Pulse, module: Module) = send(pulse)

}

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day20_input.txt")?.bufferedReader()?.readLines()!!
    val parsedLines = lines.map { parseLine(it) }
    val modules = createModules(parsedLines)
    setupConnections(parsedLines, modules)
    solvePart1(modules)
    solvePart2(modules)
}

private fun solvePart1(modules: Map<String, Module>) {
    var cycles = 0
    var lowPulses = 0
    var highPulses = 0
    val queue = LinkedList<Message>()
    while (cycles < 1000) {
        cycles++
        lowPulses++
        val messages = modules["broadcaster"]!!.accept(Pulse.LOW, modules["broadcaster"]!!)
        queue.addAll(messages)
        while (queue.isNotEmpty()) {
            val msg = queue.removeFirst()
            if (msg.pulse == Pulse.LOW) {
                lowPulses++
            } else {
                highPulses++
            }
            queue.addAll(msg.destination.accept(msg.pulse, msg.source))
        }
    }
    val resultPart1 = lowPulses * highPulses
    println("Cycles $cycles, low pulses $lowPulses, high pulses $highPulses")
    println("Result part 1: $resultPart1")
}

private fun solvePart2(modules: Map<String, Module>) {
    var cycles = 0L
    val tj = modules["tj"] as Conjunction
    val cyclesCountToHighPulseInMemory1 = LongArray(tj.memory.size) { 0 }
    val cyclesCountToHighPulseInMemory2 = LongArray(tj.memory.size) { 0 }
    var endCycles = false
    val queue = LinkedList<Message>()
    while (!endCycles) {
        cycles++
        val messages = modules["broadcaster"]!!.accept(Pulse.LOW, modules["broadcaster"]!!)
        queue.addAll(messages)
        while (queue.isNotEmpty()) {
            val msg = queue.removeFirst()
            queue.addAll(msg.destination.accept(msg.pulse, msg.source))
            if (msg.destination.name == "tj" && msg.pulse == Pulse.HIGH) {
                val index = tj.inputs.indexOf(msg.source)
                if (cyclesCountToHighPulseInMemory1[index] == 0L) {
                    cyclesCountToHighPulseInMemory1[index] = cycles
                } else if (cyclesCountToHighPulseInMemory2[index] == 0L) {
                    cyclesCountToHighPulseInMemory2[index] = cycles
                }
            }
        }
        endCycles = cyclesCountToHighPulseInMemory1.all { it != 0L } && cyclesCountToHighPulseInMemory2.all { it != 0L }
    }
    for (i in cyclesCountToHighPulseInMemory1.indices) {
        cyclesCountToHighPulseInMemory1[i] = abs(cyclesCountToHighPulseInMemory2[i] - cyclesCountToHighPulseInMemory1[i])
    }
    val cyclesPart2 = lcm(cyclesCountToHighPulseInMemory1)
    println("Cycles part 2: $cyclesPart2")
}

fun parseLine(line: String): Pair<String, Array<String>> {
    val arrowIndex = line.indexOf("->")
    val source = line.substring(0, arrowIndex-1)
    val destinations = line.substring(arrowIndex + 3).split(",")
    val outputs = Array(destinations.size) { i ->
        destinations[i].trim()
    }
    return Pair(source, outputs)
}

private fun createModules(inputLinesParsed: List<Pair<String, Array<String>>>): MutableMap<String, Module> {
    val modules = mutableMapOf<String, Module>()
    for (line in inputLinesParsed) {
        val module = when (line.first[0]) {
            '%' -> FlipFlop(line.first.substring(1))
            '&' -> Conjunction(line.first.substring(1))
            'b' -> Broadcaster()
            else -> throw IllegalArgumentException("Wrong module ${line.first}")
        }
        val key = getModuleName(line.first)
        modules[key] = module
    }
    return modules
}

private fun setupConnections(inputLinesParsed: List<Pair<String, Array<String>>>, modules: MutableMap<String, Module>) {
    for (line in inputLinesParsed) {
        val source = modules[getModuleName(line.first)]!!
        for (t in line.second) {
            if (modules.containsKey(t)) {
                source.addOutput(modules[t]!!)
            } else {
                val output = Output(t)
                source.addOutput(output)
                modules[t] = output
            }
        }
    }
}

private fun getModuleName(name: String) = when (name[0]) {
    '%', '&' -> name.substring(1)
    else -> name
}

fun lcm(numbers: LongArray): Long {
    var ans = 1L
    for (n in numbers) {
        ans = (ans * n) / gcd(n, ans)
    }
    return ans
}

fun gcd(a: Long, b: Long): Long {
    var big = max(a, b)
    var small = min(a, b)
    while (big != 0L && small != 0L) {
        val mod = big % small
        big = small
        small = mod
    }
    return max(big, small)
}
