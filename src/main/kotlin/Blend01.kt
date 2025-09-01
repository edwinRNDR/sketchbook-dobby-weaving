import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.uniform
import kotlin.random.Random

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }
        program {

            fun f0(x: Int, y: Int): Int {
                return (x + y).mod(2)
            }

            fun f1(x: Int, y: Int): Int {
                return (x / 2 - y / 4).mod(2)
            }
            extend {

                val r = Random(0)
                val w = 8

                for (y in 0 until w*8) {
                    for (x in 0 until w*8) {

                        val bx = x / (w* 8.0)


                        val v0 = f0(x, y)
                        val v1 = f1(x, y)


                        val dx =  (x.mod(8) - 3.5) / 3.5
                        val v = if (bx > (x + y*8).mod(64)/64.0) {
                            v1
                        } else {
                            v0
                        }

                        if (v == 1) {
                            drawer.fill = ColorRGBa.BLACK
                        } else {
                            drawer.fill = ColorRGBa.WHITE
                        }
                        drawer.rectangle(x * 10.0, y * 10.0, 10.0, 10.0)
                    }
                }

            }
        }
    }
}