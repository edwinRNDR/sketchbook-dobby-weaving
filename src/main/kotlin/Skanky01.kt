import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment2D
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

// A helper function to filter for values in the [0,1] interval:
private fun accept(t: Double): Boolean {
    return t in 0.0..1.0
}


// A real-cuberoots-only function:
private fun cuberoot(v: Double): Double {
    if (v < 0) return -(-v).pow(1.0 / 3);
    return v.pow(1.0 / 3)
}

internal fun approximately(a: Double, b: Double, epsilon: Double = 1E-8): Boolean {
    return abs(a - b) < epsilon
}

// Now then: given cubic coordinates {pa, pb, pc, pd} find all roots.
internal fun getCubicRoots(pa: Double, pb: Double, pc: Double, pd: Double): List<Double> {
    var a = (3 * pa - 6 * pb + 3 * pc)
    var b = (-3 * pa + 3 * pb)
    var c = pa
    val d = (-pa + 3 * pb - 3 * pc + pd);

    // do a check to see whether we even need cubic solving:
    if (approximately(d, 0.0)) {
        // this is not a cubic curve.
        if (approximately(a, 0.0)) {
            // in fact, this is not a quadratic curve either.
            if (approximately(b, 0.0)) {
                // in fact in fact, there are no solutions.
                return emptyList()
            }
            // linear solution
            return listOf(-c / b).filter(::accept)
        }
        // quadratic solution
        val q = sqrt(b * b - 4 * a * c)
        val twoA = 2 * a
        return listOf((q - b) / twoA, (-b - q) / twoA).filter(::accept)
    }

    // at this point, we know we need a cubic solution.

    a /= d
    b /= d
    c /= d

    val p = (3 * b - a * a) / 3
    val p3 = p / 3
    val q = (2 * a * a * a - 9 * a * b + 27 * c) / 27
    val q2 = q / 2
    val discriminant = q2 * q2 + p3 * p3 * p3

    // and some variables we're going to use later on:
    var u1 = 0.0
    var v1 = 0.0
    var root1 = 0.0
    var root2 = 0.0
    var root3 = 0.0

    // three possible real roots:
    if (discriminant < 0) {
        var mp3 = -p / 3
        val mp33 = mp3 * mp3 * mp3
        val r = sqrt(mp33)
        val t = -q / (2 * r)
        val cosphi = if (t < -1) -1.0 else if (t > 1) 1.0 else t
        val phi = acos(cosphi)
        val crtr = cuberoot(r)
        val t1 = 2 * crtr
        root1 = t1 * cos(phi / 3) - a / 3
        root2 = t1 * cos((phi + 2 * PI) / 3) - a / 3
        root3 = t1 * cos((phi + 4 * PI) / 3) - a / 3
        return listOf(root1, root2, root3).filter(::accept)
    }

    // three real roots, but two of them are equal:
    if (discriminant == 0.0) {
        u1 = if (q2 < 0) cuberoot(-q2) else -cuberoot(q2)
        root1 = 2 * u1 - a / 3
        root2 = -u1 - a / 3
        return listOf(root1, root2).filter(::accept)
    }

    // one real root, two complex roots
    val sd = sqrt(discriminant)
    u1 = cuberoot(sd - q2)
    v1 = cuberoot(sd + q2)
    root1 = u1 - v1 - a / 3
    return listOf(root1).filter(::accept)
}

fun Segment2D.tForX(x: Double): Double {
    if (x == start.x) return 0.0
    if (x == end.x) return 1.0

    if (linear) {
        return (x - start.x) / (end.x - start.x)
    } else {
        val cb = this.cubic
        val a = cb.start.x - x
        val b = cb.control[0].x - x
        val c = cb.control[1].x - x
        val d = cb.end.x - x

        val t = getCubicRoots(a, b, c, d).firstOrNull() ?: 0.0

        return t
    }
}

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }
        program {
            extend {

                val s = Segment2D(
                    Vector2(50.0, 0.0),
                    Vector2(300.0, 0.0),
                    Vector2(width - 300.0, height.toDouble()),

                    Vector2(width.toDouble() - 50.0, height.toDouble())
                )
                val t = s.tForX(mouse.position.x)
                drawer.stroke = ColorRGBa.PINK
                drawer.contour(s.contour)
                drawer.circle(s.position(t), 20.0)
                for (x in 0 until width step 10) {

                    if (x > s.start.x && x <= s.end.x) {
                        val t = s.tForX(x.toDouble())
                        val p = s.position(t)
                        val h = cos(seconds + x * 0.01) * 125.0 + 125.0
                        drawer.lineSegment(p - Vector2(0.0, h), p + Vector2(0.0, h))
                    }
                }

            }
        }
    }
}