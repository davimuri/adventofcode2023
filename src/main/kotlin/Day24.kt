package com.dmmapps

import org.apache.commons.math3.linear.*
import kotlin.math.*


// https://thirdspacelearning.com/gcse-maths/algebra/intersecting-lines/
// https://github.com/DeadlyRedCube/AdventOfCode/blob/1f9d0a3e3b7e7821592244ee51bce5c18cf899ff/2023/AOC2023/D24.h#L66-L294
// https://www.youtube.com/watch?v=guOyA7Ijqgk&list=PLnNm9syGLD3zLoIGWeHfnEekEKxPKLivw&index=32

const val X = 0
const val Y = 1
const val Z = 2
const val VX = 3
const val VY = 4
const val VZ = 5

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("/day24_input.txt")?.bufferedReader()?.readLines()!!
    val values = lines.map { parseLineValues(it) }
    val equations = values.map { parseLineEquation(it) }
//    val testArea = 7F..27F
    val testArea = 200000000000000F..400000000000000F
    var count = 0
    for (i in equations.indices) {
        for (j in i+1..<equations.size) {
            if (equations[i][0] != equations[j][0]) {
                val intersection = getIntersection(equations[i], equations[j])
                if (intersection.first in testArea && intersection.second in testArea
                    && !intersectionIsInPast(intersection, values[i]) && !intersectionIsInPast(intersection, values[j])) {
                    count++
                }
            }
        }
    }
    println("Part 1 - intersections $count")
    solvePart2(values)
}

fun solvePart2(hailstones: List<LongArray>) {
    val indexes = Triple(0, 1, 2)
    val solution = trySolutionPart2(hailstones, indexes)
    val mathLibSolution = trySolutionPart2WithApacheMath(hailstones, indexes)

    print("${round(solution[0])}, ${round(solution[1])}, ${round(solution[2])}")
    println(" @ ${round(solution[3])}, ${round(solution[4])}, ${round(solution[5])}")
    val answer = round(solution[0]) + round(solution[1]) + round(solution[2])
    println("Part 2 answer: ${answer.toBigDecimal().toPlainString()}")

    print("${round(mathLibSolution[0])}, ${round(mathLibSolution[1])}, ${round(mathLibSolution[2])}")
    println(" @ ${round(mathLibSolution[3])}, ${round(mathLibSolution[4])}, ${round(mathLibSolution[5])}")
    val mathLibAnswer = round(mathLibSolution[0]) + round(mathLibSolution[1]) + round(mathLibSolution[2])
    println("Part 2 answer with math lib: ${mathLibAnswer.toBigDecimal().toPlainString()}")
}
fun trySolutionPart2(hailstones: List<LongArray>, indexes: Triple<Int, Int, Int>): DoubleArray {
    // x, y, z -> coordinates
    // v -> velocity
    // h -> hailstone
    // r -> rock
    // at a specific time t, x coordinate for hailstone and rock should be the same:
    // x_h + v_xh * t = x_r + v_xr * t
    // v_xh * t - v_xr * t = x_r - x_h
    // t = (x_r - x_h) / (v_xh - v_xr)
    // doing the same for y and z
    // t = (x_r - x_h) / (v_xh - v_xr) = (y_r - y_h) / (v_yh - v_yr) = (z_r - z_h) / (v_zh - v_zr)
    // 1 and 2 -> (x_r - x_h) / (v_xh - v_xr) = (y_r - y_h) / (v_yh - v_yr)
    // 1 and 3 -> (x_r - x_h) / (v_xh - v_xr) = (z_r - z_h) / (v_zh - v_zr)
    // 2 and 3 -> (y_r - y_h) / (v_yh - v_yr) = (z_r - z_h) / (v_zh - v_zr)
    // 1 and 2 (x_r, y_r) ->
    // (x_r - x_h) * (v_yh - v_yr) = (y_r - y_h) * (v_xh - v_xr)
    // x_r * v_yh - x_r * v_yr - x_h * v_yh + x_h * v_yr = y_r * v_xh - y_r * v_xr - y_h * v_xh + y_h * v_xr
    // x_r * v_yh - x_h * v_yh + x_h * v_yr - y_r * v_xh + y_h * v_xh - y_h * v_xr = x_r * v_yr - y_r * v_xr
    // 1 and 3 (x_r, z_r) ->
    // (x_r - x_h) * (v_zh - v_zr) = (z_r - z_h) * (v_xh - v_xr)
    // x_r * v_zh - x_r * v_zr - x_h * v_zh + x_h * v_zr = z_r * v_xh - z_r * v_xr - z_h * v_xh + z_h * v_xr
    // x_r * v_zh - x_h * v_zh + x_h * v_zr - z_r * v_xh + z_h * v_xh - z_h * v_xr = x_r * v_zr - z_r * v_xr
    // 2 and 3 (y_r, z_r) ->
    // (y_r - y_h) / (v_yh - v_yr) = (z_r - z_h) / (v_zh - v_zr)
    // (y_r - y_h) * (v_zh - v_zr) = (z_r - z_h) * (v_yh - v_yr)
    // y_r * v_zh - y_r * v_zr - y_h * v_zh + y_h * v_zr = z_r * v_yh - z_r * v_yr - z_h * v_yh + z_h * v_yr
    // y_r * v_zh - y_h * v_zh + y_h * v_zr - z_r * v_yh + z_h * v_yh - z_h * v_yr = y_r * v_zr - z_r * v_yr
    // With one hailstone we get 2 formulas and 6 unknown variables
    // To find the values for the 6 unknown variables we need 6 formulas, so we need to get 3 hailstones
    // suppose we name the hailstones with A, B, C
    // so for hailstone A we know the values Ax, Ay, Az, Avx, Avy, Avz and similar for B and C
    // replacing values for hailstone A, B, C in formula we get:
    // (1) x_r * Avy - Ax * Avy + Ax * v_yr - y_r * Avx + Ay * Avx - Ay * v_xr = x_r * v_yr - y_r * v_xr
    // (2) x_r * Avz - Ax * Avz + Ax * v_zr - z_r * Avx + Az * Avx - Az * v_xr = x_r * v_zr - z_r * v_xr
    // (3) x_r * Bvy - Bx * Bvy + Bx * v_yr - y_r * Bvx + By * Bvx - By * v_xr = x_r * v_yr - y_r * v_xr
    // (4) x_r * Bvz - Bx * Bvz + Bx * v_zr - z_r * Bvx + Bz * Bvx - Bz * v_xr = x_r * v_zr - z_r * v_xr
    // (5) x_r * Cvy - Cx * Cvy + Cx * v_yr - y_r * Cvx + Cy * Cvx - Cy * v_xr = x_r * v_yr - y_r * v_xr
    // (6) x_r * Cvz - Cx * Cvz + Cx * v_zr - z_r * Cvx + Cz * Cvx - Cz * v_xr = x_r * v_zr - z_r * v_xr
    // (A) y_r * Avz - Ay * Avz + Ay * v_zr - z_r * Avy + Az * Avy - Az * v_yr = y_r * v_zr - z_r * v_yr
    // (B) y_r * Bvz - By * Bvz + By * v_zr - z_r * Bvy + Bz * Bvy - Bz * v_yr = y_r * v_zr - z_r * v_yr
    // (C) y_r * Cvz - Cy * Cvz + Cy * v_zr - z_r * Cvy + Cz * Cvy - Cz * v_yr = y_r * v_zr - z_r * v_yr

    // mixing formula for (1) and (3), since right part is the same:
    // x_r * Avy - Ax * Avy + Ax * v_yr - y_r * Avx + Ay * Avx - Ay * v_xr = x_r * Bvy - Bx * Bvy + Bx * v_yr - y_r * Bvx + By * Bvx - By * v_xr
    // (Avy - Bvy) * x_r - Ax * Avy + (Ax - Bx) * v_yr + (Bvx - Avx) * y_r + Ay * Avx + (By - Ay) * v_xr = - Bx * Bvy + By * Bvx
    // (Avy - Bvy) * x_r + (Ax - Bx) * v_yr + (Bvx - Avx) * y_r + (By - Ay) * v_xr = - Bx * Bvy + By * Bvx + Ax * Avy - Ay * Avx

    // (1) and (3) -> (Avy - Bvy)*x_r + (Bvx - Avx)*y_r + (By - Ay)*v_xr + (Ax - Bx)*v_yr = By * Bvx - Bx * Bvy + Ax * Avy - Ay * Avx
    // (1) and (5) -> (Avy - Cvy)*x_r + (Cvx - Avx)*y_r + (Cy - Ay)*v_xr + (Ax - Cx)*v_yr = Cy * Cvx - Cx * Cvy + Ax * Avy - Ay * Avx
    // NO!! (3) and (5) -> (Bvy - Cvy)*x_r + (Cvx - Bvx)*y_r + (Cy - By)*v_xr + (Bx - Cx)*v_yr = Cy * Cvx - Cx * Cvy + Bx * Bvy - By * Bvx

    // (2) and (4) ->  x_r * Avz - Ax * Avz + Ax * v_zr - z_r * Avx + Az * Avx - Az * v_xr = x_r * Bvz - Bx * Bvz + Bx * v_zr - z_r * Bvx + Bz * Bvx - Bz * v_xr
    // (Avz - Bvz)*x_r - Ax * Avz + Ax * v_zr - z_r * Avx + Az * Avx - Az * v_xr =  - Bx * Bvz + Bx * v_zr - z_r * Bvx + Bz * Bvx - Bz * v_xr
    // (Avz - Bvz)*x_r - Ax * Avz + (Ax - Bx)* v_zr - z_r * Avx + Az * Avx - Az * v_xr =  - Bx * Bvz - z_r * Bvx + Bz * Bvx - Bz * v_xr
    // (Avz - Bvz)*x_r - Ax * Avz + (Ax - Bx)* v_zr + (Bvx - Avx)*z_r + Az * Avx - Az * v_xr =  - Bx * Bvz + Bz * Bvx - Bz * v_xr
    // (Avz - Bvz)*x_r - Ax * Avz + (Ax - Bx)* v_zr + (Bvx - Avx)*z_r + Az * Avx + (Bz - Az)*v_xr = - Bx * Bvz + Bz * Bvx
    //                (Avz - Bvz)*x_r + (Bvx - Avx)*z_r + (Bz - Az)*v_xr + (Ax - Bx)*v_zr = - Bx * Bvz + Bz * Bvx + Ax * Avz - Az * Avx

    // (2) and (4) -> (Avz - Bvz)*x_r + (Bvx - Avx)*z_r + (Bz - Az)*v_xr + (Ax - Bx)*v_zr = Bz * Bvx - Bx * Bvz + Ax * Avz - Az * Avx
    // (2) and (6) -> (Avz - Cvz)*x_r + (Cvx - Avx)*z_r + (Cz - Az)*v_xr + (Ax - Cx)*v_zr = Cz * Cvx - Cx * Cvz + Ax * Avz - Az * Avx
    // NO!! (4) and (6) -> (Bvz - Cvz)*x_r + (Cvx - Bvx)*z_r + (Cz - Bz)*v_xr + (Bx - Cx)*v_zr = Cz * Cvx - Cx * Cvz + Bx * Bvz - Bz * Bvx

    // (A) and (B) -> y_r * Avz - Ay * Avz + Ay * v_zr - z_r * Avy + Az * Avy - Az * v_yr = y_r * Bvz - By * Bvz + By * v_zr - z_r * Bvy + Bz * Bvy - Bz * v_yr
    // (Avz - Bvz)*y_r + (Bvy - Avy)*z_r + (Bz - Az)*v_yr + (Ay - By)*v_zr = Bz * Bvy - By * Bvz + Ay * Avz - Az * Avy
    // (A) and (C) -> y_r * Avz - Ay * Avz + Ay * v_zr - z_r * Avy + Az * Avy - Az * v_yr = y_r * Cvz - Cy * Cvz + Cy * v_zr - z_r * Cvy + Cz * Cvy - Cz * v_yr
    // (Avz - Cvz)*y_r + (Cvy - Avy)*z_r + (Cz - Az)*v_yr + (Ay - Cy)*v_zr = Ay * Avz - Cy * Cvz + Cz * Cvy - Az * Avy

    val aIndex = indexes.first
    val bIndex = indexes.second
    val cIndex = indexes.third

    val hA = hailstones[aIndex].map { it.toDouble() }
    val hB = hailstones[bIndex].map { it.toDouble() }
    val hC = hailstones[cIndex].map { it.toDouble() }

    // x_r coefficients
    val aBvy = hA[VY] - hB[VY] // Avy - Bvy
    val aCvy = hA[VY] - hC[VY] // Avy - Cvy
    val aBvz = hA[VZ] - hB[VZ] // Avz - Bvz
    val aCvz = hA[VZ] - hC[VZ] // Avz - Cvz

    // y_r and z_r coefficients
    val bAvx = hB[VX] - hA[VX] // Bvx - Avx
    val cAvx = hC[VX] - hA[VX] // Cvx - Avx

    // z_r coefficients
    val bAvy = hB[VY] - hA[VY] // Bvy - Avy
    val cAvy = hC[VY] - hA[VY] // Cvy - Avy

    // v_xr coefficients
    val bAy = hB[Y] - hA[Y] // By - Ay
    val cAy = hC[Y] - hA[Y] // Cy - Ay
    val bAz = hB[Z] - hA[Z] // Bz - Az
    val cAz = hC[Z] - hA[Z] // Cz - Az

    // v_yr and v_zr coefficients
    val aBx = hA[X] - hB[X] // Ax - Bx
    val aCx = hA[X] - hC[X] // Ax - Cx
    val aBy = hA[Y] - hB[Y] // Ay - By
    val aCy = hA[Y] - hC[Y] // Ay - Cy

    val ct1 = hB[Y] * hB[VX] - hB[X] * hB[VY] + hA[X] * hA[VY] - hA[Y] * hA[VX] // By * Bvx - Bx * Bvy + Ax * Avy - Ay * Avx
    val ct2 = hC[Y] * hC[VX] - hC[X] * hC[VY] + hA[X] * hA[VY] - hA[Y] * hA[VX] // Cy * Cvx - Cx * Cvy + Ax * Avy - Ay * Avx
    val ct4 = hB[Z] * hB[VX] - hB[X] * hB[VZ] + hA[X] * hA[VZ] - hA[Z] * hA[VX] // Bz * Bvx - Bx * Bvz + Ax * Avz - Az * Avx
    val ct5 = hC[Z] * hC[VX] - hC[X] * hC[VZ] + hA[X] * hA[VZ] - hA[Z] * hA[VX] // Cz * Cvx - Cx * Cvz + Ax * Avz - Az * Avx
    val ct7 = hB[Z] * hB[VY] - hB[Y] * hB[VZ] + hA[Y] * hA[VZ] - hA[Z] * hA[VY] // Bz * Bvy - By * Bvz + Ay * Avz - Az * Avy
    val ct8 = hA[Y] * hA[VZ] - hC[Y] * hC[VZ] + hC[Z] * hC[VY] - hA[Z] * hA[VY] // Ay * Avz - Cy * Cvz + Cz * Cvy - Az * Avy

    // same 6 formulas written with the coefficients and constants:
    // (7) aBvy * x_r + bAvx * y_r + bAy * v_xr + aBx * v_yr = ct1
    // (8) aCvy * x_r + cAvx * y_r + cAy * v_xr + aCx * v_yr = ct2
    // (10) aBvz * x_r + bAvx * z_r + bAz * v_xr + aBx * v_zr = ct4
    // (11) aCvz * x_r + cAvx * z_r + cAz * v_xr + aCx * v_zr = ct5
    // (9) aBvz * y_r + bAvy * z_r + bAz * v_yr + aBy * v_zr = ct7
    // (12) aCvz * y_r + cAvy * z_r + cAz * v_yr + aCy * v_zr = ct8

    // (7) and (8) to get (13) y_r
    // (7) aBvy * x_r + bAvx * y_r + bAy * v_xr + aBx * v_yr = ct1
    // x_r = (ct1 - bAvx * y_r - bAy * v_xr - aBx * v_yr) / aBvy

    // (8) aCvy * x_r + cAvx * y_r + cAy * v_xr + aCx * v_yr = ct2
    // x_r = (ct2 - cAvx * y_r - cAy * v_xr - aCx * v_yr) / aCvy

    // (ct1 - bAvx * y_r - bAy * v_xr - aBx * v_yr) / aBvy = (ct2 - cAvx * y_r - cAy * v_xr - aCx * v_yr) / aCvy
    // (ct1 - bAvx * y_r - bAy * v_xr - aBx * v_yr) * aCvy = (ct2 - cAvx * y_r - cAy * v_xr - aCx * v_yr) * aBvy
    // ct1*aCvy - bAvx*aCvy * y_r - bAy*aCvy * v_xr - aBx*aCvy * v_yr = ct2*aBvy - cAvx*aBvy * y_r - cAy*aBvy * v_xr - aCx*aBvy * v_yr
    // cAvx*aBvy * y_r - bAvx*aCvy * y_r = ct2*aBvy - cAy*aBvy * v_xr - aCx*aBvy * v_yr - ct1*aCvy + bAy*aCvy * v_xr + aBx*aCvy * v_yr
    // (cAvx*aBvy - bAvx*aCvy) * y_r = (bAy*aCvy - cAy*aBvy) * v_xr + (aBx*aCvy - aCx*aBvy) * v_yr + ct2*aBvy - ct1*aCvy
    // (13) y_r = [(bAy*aCvy - cAy*aBvy) * v_xr + (aBx*aCvy - aCx*aBvy) * v_yr + ct2*aBvy - ct1*aCvy] / (cAvx*aBvy - bAvx*aCvy)

    // (10) and (11) to get (14) x_r
    // (10) aBvz * x_r + bAvx * z_r + bAz * v_xr + aBx * v_zr = ct4
    //  z_r = (ct4 - aBvz * x_r - bAz * v_xr - aBx * v_zr) / bAvx

    // (11) aCvz * x_r + cAvx * z_r + cAz * v_xr + aCx * v_zr = ct5
    // z_r = (ct5 - aCvz * x_r - cAz * v_xr - aCx * v_zr) / cAvx

    // (ct4 - aBvz * x_r - bAz * v_xr - aBx * v_zr) / bAvx = (ct5 - aCvz * x_r - cAz * v_xr - aCx * v_zr) / cAvx
    // (14) x_r = [(bAz*cAvx - cAz*bAvx) * v_xr + (aBx*cAvx - aCx*bAvx) * v_zr + ct5*bAvx - ct4*cAvx] / (aCvz*bAvx - aBvz*cAvx)

    // (9) and (12) to get (15) z_r
    // (9) aBvz * y_r + bAvy * z_r + bAz * v_yr + aBy * v_zr = ct7
    // y_r = (ct7 - bAvy * z_r - bAz * v_yr - aBy * v_zr) / aBvz

    // (12) aCvz * y_r + cAvy * z_r + cAz * v_yr + aCy * v_zr = ct8
    // y_r = (ct8 - cAvy * z_r - cAz * v_yr - aCy * v_zr) / aCvz

    // (ct7 - bAvy * z_r - bAz * v_yr - aBy * v_zr) / aBvz = (ct8 - cAvy * z_r - cAz * v_yr - aCy * v_zr) / aCvz
    // (15) z_r = [(bAz*aCvz - cAz*aBvz) * v_yr + (aBy*aCvz - aCy*aBvz) * v_zr + ct8*aBvz - ct7*aCvz] / (cAvy*aBvz - bAvy*aCvz)

    // rewrite (13), (14) and (15) using new coefficients:
    // (13) y_r = [(bAy*aCvy - cAy*aBvy) * v_xr + (aBx*aCvy - aCx*aBvy) * v_yr + ct2*aBvy - ct1*aCvy] / (cAvx*aBvy - bAvx*aCvy)
    // (14) x_r = [(bAz*cAvx - cAz*bAvx) * v_xr + (aBx*cAvx - aCx*bAvx) * v_zr + ct5*bAvx - ct4*cAvx] / (aCvz*bAvx - aBvz*cAvx)
    // (15) z_r = [(bAz*aCvz - cAz*aBvz) * v_yr + (aBy*aCvz - aCy*aBvz) * v_zr + ct8*aBvz - ct7*aCvz] / (cAvy*aBvz - bAvy*aCvz)
    val c13vxr = bAy*aCvy - cAy*aBvy
    val c13vyr = aBx*aCvy - aCx*aBvy
    val c13c1 = ct2*aBvy - ct1*aCvy
    val c13c2 = cAvx*aBvy - bAvx*aCvy
    val c14vxr = bAz*cAvx - cAz*bAvx
    val c14vzr = aBx*cAvx - aCx*bAvx
    val c14c1 = ct5*bAvx - ct4*cAvx
    val c14c2 = aCvz*bAvx - aBvz*cAvx
    val c15vyr = bAz*aCvz - cAz*aBvz
    val c15vzr = aBy*aCvz - aCy*aBvz
    val c15c1 = ct8*aBvz - ct7*aCvz
    val c15c2 = cAvy*aBvz - bAvy*aCvz
    // (13) y_r = (c13vxr * v_xr + c13vyr * v_yr + c13c1) / c13c2
    // (14) x_r = (c14vxr * v_xr + c14vzr * v_zr + c14c1) / c14c2
    // (15) z_r = (c15vyr * v_yr + c15vzr * v_zr + c15c1) / c15c2

    // substitution of x_r (14) and y_r (13) in (7)
    // (7) aBvy * x_r + bAvx * y_r + bAy * v_xr + aBx * v_yr = ct1
    // aBvy * [(c14vxr * v_xr + c14vzr * v_zr + c14c1) / c14c2] +
    //     bAvx * [(c13vxr * v_xr + c13vyr * v_yr + c13c1) / c13c2] +
    //     bAy * v_xr + aBx * v_yr = ct1
    // (aBvy/c14c2) * c14vxr * v_xr + (aBvy/c14c2) * c14vzr * v_zr + (aBvy/c14c2) * c14c1 +
    //     (bAvx/c13c2) * c13vxr * v_xr + (bAvx/c13c2) * c13vyr * v_yr + (bAvx/c13c2) * c13c1 +
    //     bAy * v_xr + aBx * v_yr = ct1
    // [(aBvy/c14c2) * c14vxr + (bAvx/c13c2) * c13vxr + bAy] * v_xr +
    //    [(bAvx/c13c2) * c13vyr + aBx] * v_yr +
    //    (aBvy/c14c2) * c14vzr * v_zr
    //    = ct1 - (bAvx/c13c2) * c13c1 - (aBvy/c14c2) * c14c1
    // rewriting previous formula with new coefficients
    val c16vxr = (aBvy/c14c2) * c14vxr + (bAvx/c13c2) * c13vxr + bAy
    val c16vyr = (bAvx/c13c2) * c13vyr + aBx
    val c16vzr = (aBvy/c14c2) * c14vzr
    val c16c1 = ct1 - (bAvx/c13c2) * c13c1 - (aBvy/c14c2) * c14c1
    // (16) c16vxr * v_xr + c16vyr * v_yr + c16vzr * v_zr = c16c1

    // substitution of y_r (13) and z_r (15) in (9)
    // (9) aBvz * y_r + bAvy * z_r + bAz * v_yr + aBy * v_zr = ct7
    // aBvz * [(c13vxr * v_xr + c13vyr * v_yr + c13c1) / c13c2] +
    //     bAvy * [(c15vyr * v_yr + c15vzr * v_zr + c15c1) / c15c2] +
    //     bAz * v_yr + aBy * v_zr = ct7
    //  (aBvz/c13c2) * c13vxr * v_xr +
    //     [(aBvz/c13c2) * c13vyr + (bAvy/c15c2) * c15vyr + bAz] * v_yr +
    //     [(bAvy/c15c2) * c15vzr + aBy] * v_zr
    //     = ct7 - (aBvz/c13c2) * c13c1 - (bAvy/c15c2) * c15c1
    // rewriting previous formula with new coefficients
    val c17vxr = (aBvz/c13c2) * c13vxr
    val c17vyr = (aBvz/c13c2) * c13vyr + (bAvy/c15c2) * c15vyr + bAz
    val c17vzr = (bAvy/c15c2) * c15vzr + aBy
    val c17c1 = ct7 - (aBvz/c13c2) * c13c1 - (bAvy/c15c2) * c15c1
    // (17) c17vxr * v_xr + c17vyr * v_yr + c17vzr * v_zr = c17c1

    // substitution of x_r (14) and z_r (15) in (10)
    // (14) x_r = (c14vxr * v_xr + c14vzr * v_zr + c14c1) / c14c2
    // (15) z_r = (c15vyr * v_yr + c15vzr * v_zr + c15c1) / c15c2
    // (10) aBvz * x_r + bAvx * z_r + bAz * v_xr + aBx * v_zr = ct4
    // aBvz * [(c14vxr * v_xr + c14vzr * v_zr + c14c1) / c14c2] +
    //     bAvx * [(c15vyr * v_yr + c15vzr * v_zr + c15c1) / c15c2] +
    //     bAz * v_xr + aBx * v_zr = ct4
    // [(aBvz/c14c2) * c14vxr + bAz] * v_xr + (bAvx/c15c2) * c15vyr * v_yr +
    //     [(aBvz/c14c2) * c14vzr + (bAvx/c15c2) * c15vzr + aBx] * v_zr
    //     = ct4 - (aBvz/c14c2) * c14c1 - (bAvx/c15c2) * c15c1
    // rewriting previous formula with new coefficients
    // rewriting previous formula with new coefficients
    val c18vxr = (aBvz/c14c2) * c14vxr + bAz
    val c18vyr = (bAvx/c15c2) * c15vyr
    val c18vzr = (aBvz/c14c2) * c14vzr + (bAvx/c15c2) * c15vzr + aBx
    val c18c1 = ct4 - (aBvz/c14c2) * c14c1 - (bAvx/c15c2) * c15c1
    // (18) c18vxr * v_xr + c18vyr * v_yr + c18vzr * v_zr = c18c1

    // We have 3 formulas with 3 variables:
    // (16) c16vxr * v_xr + c16vyr * v_yr + c16vzr * v_zr = c16c1
    // (17) c17vxr * v_xr + c17vyr * v_yr + c17vzr * v_zr = c17c1
    // (18) c18vxr * v_xr + c18vyr * v_yr + c18vzr * v_zr = c18c1

    // From (16):
    // (c16vyr * v_yr + c16vzr * v_zr - c16c1) / - c16vxr = v_xr
    // From (17):
    // (c17vyr * v_yr + c17vzr * v_zr - c17c1) / - c17vxr =  v_xr
    // matching 2 previous formulas:
    // (c16vyr * v_yr + c16vzr * v_zr - c16c1) / - c16vxr = (c17vyr * v_yr + c17vzr * v_zr - c17c1) / - c17vxr
    // (c16vxr * c17vyr - c17vxr * c16vyr) * v_yr + (c16vxr * c17vzr - c17vxr * c16vzr) * v_zr = c16vxr * c17c1 - c17vxr * c16c1
    // rewriting previous formula with new coefficients
    val c19vyr = c16vxr * c17vyr - c17vxr * c16vyr
    val c19vzr = c16vxr * c17vzr - c17vxr * c16vzr
    val c19 = c16vxr * c17c1 - c17vxr * c16c1
    // (19) c19vyr * v_yr + c19vzr * v_zr = c19

    // Doing the same with (16) and (18):
    // (c16vxr * c18vyr - c18vxr * c16vyr) * v_yr + (c16vxr * c18vzr - c18vxr * c16vzr) * v_zr = c16vxr * c18c1 - c18vxr * c16c1
    // rewriting previous formula with new coefficients
    val c20vyr = c16vxr * c18vyr - c18vxr * c16vyr
    val c20vzr = c16vxr * c18vzr - c18vxr * c16vzr
    val c20 = c16vxr * c18c1 - c18vxr * c16c1
    // (20) c20vyr * v_yr + c20vzr * v_zr = c20

    // From (19):
    // (19) c19vyr * v_yr + c19vzr * v_zr = c19
    // v_yr = (c19 - c19vzr * v_zr) / c19vyr
    // From (20):
    // (20) c20vyr * v_yr + c20vzr * v_zr = c20
    // v_yr = (c20 - c20vzr * v_zr) / c20vyr
    // matching both:
    // (c19 - c19vzr * v_zr) / c19vyr = (c20 - c20vzr * v_zr) / c20vyr
    val vzr = (c19vyr * c20 - c20vyr * c19) / (c19vyr * c20vzr - c20vyr * c19vzr)
    // substitution of v_zr to get v_yr
    val vyr = (c19 - c19vzr * vzr) / c19vyr
    // From (16): (c16vyr * v_yr + c16vzr * v_zr - c16c1) / - c16vxr = v_xr
    val vxr = (c16vyr * vyr + c16vzr * vzr - c16c1) / (-c16vxr)

    // Calculate the x, y, z for the rock:
    // (13) y_r = (c13vxr * v_xr + c13vyr * v_yr + c13c1) / c13c2
    val yr = (c13vxr * vxr + c13vyr * vyr + c13c1) / c13c2
    // (14) x_r = (c14vxr * v_xr + c14vzr * v_zr + c14c1) / c14c2
    val xr = (c14vxr * vxr + c14vzr * vzr + c14c1) / c14c2
    // (15) z_r = (c15vyr * v_yr + c15vzr * v_zr + c15c1) / c15c2
    val zr = (c15vyr * vyr + c15vzr * vzr + c15c1) / c15c2

    return doubleArrayOf(xr, yr, zr, vxr, vyr, vzr)
}

fun trySolutionPart2WithApacheMath(hailstones: List<LongArray>, indexes: Triple<Int, Int, Int>): DoubleArray {
    val aIndex = indexes.first
    val bIndex = indexes.second
    val cIndex = indexes.third

    val hA = hailstones[aIndex].map { it.toDouble() }
    val hB = hailstones[bIndex].map { it.toDouble() }
    val hC = hailstones[cIndex].map { it.toDouble() }

    // x_r coefficients
    val aBvy = hA[VY] - hB[VY] // Avy - Bvy
    val aCvy = hA[VY] - hC[VY] // Avy - Cvy
    val aBvz = hA[VZ] - hB[VZ] // Avz - Bvz
    val aCvz = hA[VZ] - hC[VZ] // Avz - Cvz

    // y_r and z_r coefficients
    val bAvx = hB[VX] - hA[VX] // Bvx - Avx
    val cAvx = hC[VX] - hA[VX] // Cvx - Avx

    // z_r coefficients
    val bAvy = hB[VY] - hA[VY] // Bvy - Avy
    val cAvy = hC[VY] - hA[VY] // Cvy - Avy

    // v_xr coefficients
    val bAy = hB[Y] - hA[Y] // By - Ay
    val cAy = hC[Y] - hA[Y] // Cy - Ay
    val bAz = hB[Z] - hA[Z] // Bz - Az
    val cAz = hC[Z] - hA[Z] // Cz - Az

    // v_yr and v_zr coefficients
    val aBx = hA[X] - hB[X] // Ax - Bx
    val aCx = hA[X] - hC[X] // Ax - Cx
    val aBy = hA[Y] - hB[Y] // Ay - By
    val aCy = hA[Y] - hC[Y] // Ay - Cy

    val ct1 = hB[Y] * hB[VX] - hB[X] * hB[VY] + hA[X] * hA[VY] - hA[Y] * hA[VX] // By * Bvx - Bx * Bvy + Ax * Avy - Ay * Avx
    val ct2 = hC[Y] * hC[VX] - hC[X] * hC[VY] + hA[X] * hA[VY] - hA[Y] * hA[VX] // Cy * Cvx - Cx * Cvy + Ax * Avy - Ay * Avx
    val ct4 = hB[Z] * hB[VX] - hB[X] * hB[VZ] + hA[X] * hA[VZ] - hA[Z] * hA[VX] // Bz * Bvx - Bx * Bvz + Ax * Avz - Az * Avx
    val ct5 = hC[Z] * hC[VX] - hC[X] * hC[VZ] + hA[X] * hA[VZ] - hA[Z] * hA[VX] // Cz * Cvx - Cx * Cvz + Ax * Avz - Az * Avx
    val ct7 = hB[Z] * hB[VY] - hB[Y] * hB[VZ] + hA[Y] * hA[VZ] - hA[Z] * hA[VY] // Bz * Bvy - By * Bvz + Ay * Avz - Az * Avy
    val ct8 = hA[Y] * hA[VZ] - hC[Y] * hC[VZ] + hC[Z] * hC[VY] - hA[Z] * hA[VY] // Ay * Avz - Cy * Cvz + Cz * Cvy - Az * Avy

    // same 6 formulas written with the coefficients and constants:
    // (7) aBvy * x_r + bAvx * y_r + bAy * v_xr + aBx * v_yr = ct1
    // (8) aCvy * x_r + cAvx * y_r + cAy * v_xr + aCx * v_yr = ct2
    // NO!! (9) bCvy * x_r + cBvx * y_r + cBy * v_xr + bCx * v_yr = ct3
    // (10) aBvz * x_r + bAvx * z_r + bAz * v_xr + aBx * v_zr = ct4
    // (11) aCvz * x_r + cAvx * z_r + cAz * v_xr + aCx * v_zr = ct5
    // NO!! (12) bCvz * x_r + cBvx * z_r + cBz * v_xr + bCx * v_zr = ct6
    // aBvz * y_r + bAvy * z_r + bAz * v_yr + aBy * v_zr = ct7
    // aCvz * y_r + cAvy * z_r + cAz * v_yr + aCy * v_zr = ct8

    // same 6 formulas written with the coefficients and constants:
    // (7) aBvy * x_r + bAvx * y_r              + bAy * v_xr + aBx * v_yr              = ct1
    // (8) aCvy * x_r + cAvx * y_r              + cAy * v_xr + aCx * v_yr              = ct2
    // (10) aBvz * x_r             + bAvx * z_r + bAz * v_xr              + aBx * v_zr = ct4
    // (11) aCvz * x_r             + cAvx * z_r + cAz * v_xr              + aCx * v_zr = ct5
    //                  aBvz * y_r + bAvy * z_r              + bAz * v_yr + aBy * v_zr = ct7
    //                  aCvz * y_r + cAvy * z_r              + cAz * v_yr + aCy * v_zr = ct8

    val coefficients: RealMatrix =
        Array2DRowRealMatrix(
            arrayOf(
                //            x_r   y_r   z_r  v_xr v_yr v_zr
                doubleArrayOf(aBvy, bAvx, 0.0, bAy, aBx, 0.0),
                doubleArrayOf(aCvy, cAvx, 0.0, cAy, aCx, 0.0),
                doubleArrayOf(aBvz, 0.0, bAvx, bAz, 0.0, aBx),
                doubleArrayOf(aCvz, 0.0, cAvx, cAz, 0.0, aCx),
                doubleArrayOf(0.0, aBvz, bAvy, 0.0, bAz, aBy),
                doubleArrayOf(0.0, aCvz, cAvy, 0.0, cAz, aCy)
            ),
            false
        )
    val solver = LUDecomposition(coefficients).solver
//    val solver = QRDecomposition(coefficients).solver
    val constants: RealVector = ArrayRealVector(
        doubleArrayOf(ct1, ct2, ct4, ct5, ct7, ct8),
        false
    )
    try {
        val solution = solver.solve(constants)
        return doubleArrayOf(
            solution.getEntry(0),
            solution.getEntry(1),
            solution.getEntry(2),
            solution.getEntry(3),
            solution.getEntry(4),
            solution.getEntry(5)
        )
    } catch (e: SingularMatrixException) {
        println("Singular matrix")
        return doubleArrayOf()
    }
}

fun parseLineValues(line: String) = line.replace('@', ',').split(",").map { it.trim().toLong() }.toLongArray()

fun parseLineEquation(tokens: LongArray) = getLineEquation(tokens[0], tokens[1], tokens[3], tokens[4])

fun getIntersection(line1: FloatArray, line2: FloatArray): Pair<Float, Float> {
    val combined = combine(line1, line2)
    if (combined[0] == 0F) {
        return Pair(getX(line1, combined[2]), combined[2])
    }
    return Pair(combined[2], getY(line1, combined[2]))
}

fun intersectionIsInPast(intersection: Pair<Float, Float>, lineValues: LongArray): Boolean {
    if (lineValues[3] > 0 && lineValues[0] > intersection.first) {
        return true
    }
    if (lineValues[3] < 0 && lineValues[0] < intersection.first) {
        return true
    }
    if (lineValues[4] > 0 && lineValues[1] > intersection.second) {
        return true
    }
    if (lineValues[4] < 0 && lineValues[1] < intersection.second) {
        return true
    }
    return false
}

fun getLineEquation(x: Long, y: Long, vx: Long, vy: Long): FloatArray {
    val gradient = vy.toFloat() / vx.toFloat()
    val constant = y - (vy * x).toFloat() / vx.toFloat()
    return floatArrayOf(-gradient, 1F, constant)
}

fun combine(line1: FloatArray, line2: FloatArray): FloatArray {
    var newLine1 = line1
    var newLine2 = line2
    var factor = max(line1[0].absoluteValue, line2[0].absoluteValue) / min(line1[0].absoluteValue, line2[0].absoluteValue)
    if (line1[0] * line2[0] > 0) {
        factor *= -1F
    }
    if (line1[0].absoluteValue < line2[0].absoluteValue) {
        newLine1 = line1.map { it * factor }.toFloatArray()
    } else {
        newLine2 = line2.map { it * factor }.toFloatArray()
    }
    val y = newLine1[1] + newLine2[1]
    val constant = newLine1[2] + newLine2[2]
    return floatArrayOf(0F, 1F, constant / y)
}

fun getX(line: FloatArray, y: Float)  = (line[2] - line[1]*y) / line[0]

fun getY(line: FloatArray, x: Float)  = (line[2] - line[0]*x) / line[1]
