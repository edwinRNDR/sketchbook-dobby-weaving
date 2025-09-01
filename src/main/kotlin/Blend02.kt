import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.camera.Camera2D
import org.openrndr.extra.noise.uniform
import org.openrndr.math.map
import kotlin.random.Random

fun main() {
    application {
        configure {
            width = 640
            height = 640
        }
        program {

            fun f0(x: Int, y: Int): Int {
                return (x + y).mod(2)
            }

            fun f1(x: Int, y: Int): Int {
                return (x / 2 - y / 4).mod(2)
            }

            fun f2(x: Int, y: Int): Int {
                return (x / 4 - y / 2).mod(2)
            }
            fun f3(x: Int, y: Int): Int {
                return (x*y).mod(2)
            }
            extend(Camera2D())
            extend {

                val r = Random(0)
                val w = 8

                for (y in 0 until w * 8) {
                    for (x in 0 until w * 8) {

                        val bx = (x / (w * 8.0)).map(0.25,0.75, 0.0, 1.0, clamp = true)
                        val by = (y / (w * 8.0)).map(0.25,0.75, 0.0, 1.0, clamp = true)


                        val cx = if (bx > (x + y * 8).mod(64) / 64.0) {
                            0
                        } else {
                            1
                        }

                        val cy = if (by > (x + y * 8).mod(64) / 64.0) {
                            0
                        } else {
                            1
                        }

                        val c = cx + cy * 2

                        val v = when (c) {
                            0 ->f0(x, y)
                            1 ->f1(x, y)
                            2 ->f2(x, y)
                            3 ->f3(x, y)
                            else -> error("no such case")
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