package lib

fun wstairs(i: Int, length: Int): Int {
    return i.mod(length)
}

fun wtriangle(x : Int, p: Int): Int {
        val m2 = x.mod(p * 2 - 2)
        return if (m2 >= p) {
            (p - 1) - (m2 - p + 1)
        } else {
            m2
        }.mod(p)
}

fun wsaw(x : Int, p: Int): Int {
    return x.mod(p)
}