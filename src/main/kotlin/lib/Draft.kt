package lib

import kotlinx.serialization.Serializable

@Serializable
class Draft(
    val width: Int,
    val warps: Int,
    val wefts: Int,
    val tieUp: Array<Array<Boolean>>, val drafting: IntArray, val threadling: IntArray) {
}