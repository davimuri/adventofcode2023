package com.dmmapps

fun main() {
//    val input = "rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7"
    val lines = object {}.javaClass.getResourceAsStream("/day15_input.txt")?.bufferedReader()?.readLines()!!
    val input = lines[0]
    val tokens = input.split(",")
    val sum = tokens.sumOf { hash(it) }
    println("Sum part 1: $sum")
    val sumPart2 = executeSteps(tokens)
    println("Sum part 2: $sumPart2")
}

fun executeSteps(input:List<String>): Long {
    val boxes = Array<LinkedHashMap<String, Int>>(256) {
        java.util.LinkedHashMap()
    }
    input.forEach { step ->
        if (step.last() == '-') {
            val label = step.substring(0, step.length-1)
            val box = boxes[hash(label)]
            box.remove(label)
        } else {
            val index = step.indexOf('=')
            val label = step.substring(0, index)
            val focalLength = step.substring(index+1).toInt()
            val box = boxes[hash(label)]
            box[label] = focalLength
        }
    }
    var sum = 0L
    for (i in boxes.indices) {
        boxes[i].values.forEachIndexed {j, focalLength ->
            sum += (i+1) * (j+1) * focalLength
        }
    }
    return sum
}

fun hash(input: String): Int {
    var currentValue = 0
    input.forEach {
        currentValue += it.code
        currentValue *= 17
        currentValue %= 256
    }
    return currentValue
}
