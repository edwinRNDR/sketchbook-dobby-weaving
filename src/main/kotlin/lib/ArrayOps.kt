package lib

fun pushOut(array: IntArray, index: Int, left:Int, right:Int = left, fill: Int = 0) : IntArray {

    val result = Array(array.size) { 0 }

    for (i in 0 until array.size) {

        if (i < index) {
            result[i] = if (i + right < index) array[i+right] else fill
        } else {
            result[i] = if (i -left >= index ) array[i-left] else fill
        }


    }

    return result.toIntArray()

}