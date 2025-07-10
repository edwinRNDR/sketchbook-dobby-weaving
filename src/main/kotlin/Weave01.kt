import org.openrndr.KeyModifier
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.loadFont
import org.openrndr.drawImage
import org.openrndr.events.listen
import org.openrndr.extra.textwriter.writer
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.shape.Rectangle

fun main() {
    application {
        configure {
            width = 1800
            height = 1100
            windowResizable = true
        }
        program {

            val textImage = drawImage(180, 100, contentScale = 1.0) {
                drawer.clear(ColorRGBa.BLACK)
                drawer.fontMap = loadFont("data/fonts/Roboto-Regular.ttf", 48.0)

                drawer.writer {
                    box = drawer.bounds
                    verticalAlign = 0.5
                    horizontalAlign = 0.5
                    text("CLIMATE CHANGE")
                }
            }


            val s = textImage.shadow
            s.download()
            val warps = 180
            val wefts = 100
            val width = 8

            val tieUp  = Array(width) { Array(width) { false } }

            // vertical
            val threadling = IntArray(wefts) { (it.mod(5)+it/15).mod(width)}
            // horizontal
            val drafting =IntArray(warps) { (it.mod(5)+it/15).mod(width)}


            val tieUpRect = Rectangle(0.0, 0.0, width * 10.0, width * 10.0)
            val draftingRect = Rectangle((width + 1) * 10.0, 0.0, warps * 10.0, width * 10.0)
            val threadlingRect = Rectangle(0.0, (width + 1) * 10.0, width * 10.0, wefts * 10.0)

            mouse.dragged.listen {
                if (mouse.position in tieUpRect) {
                    val j = mouse.position.y.toInt() / 10
                    val i = mouse.position.x.toInt() / 10
                    tieUp[j][i] = if (it.modifiers.contains(KeyModifier.ALT)) false else true
                }

                if (mouse.position in draftingRect) {
                    val m = mouse.position - draftingRect.corner
                    val i = m.x.toInt() / 10
                    val j = m.y.toInt() / 10
                    drafting[i] = j
                }

                if (mouse.position in threadlingRect) {
                    val n = mouse.position - threadlingRect.corner
                    val j = n.y.toInt() / 10
                    val i = n.x.toInt() / 10
                    threadling[j] = i
                }

            }

            listOf(mouse.buttonUp).listen {
                if (mouse.position in tieUpRect) {
                    val j = mouse.position.y.toInt() / 10
                    val i = mouse.position.x.toInt() / 10
                    tieUp[j][i] = !tieUp[j][i]
                }

                if (mouse.position in draftingRect) {
                    val m = mouse.position - draftingRect.corner
                    val i = m.x.toInt() / 10
                    val j = m.y.toInt() / 10
                    drafting[i] = j
                }

                if (mouse.position in threadlingRect) {
                    val n = mouse.position - threadlingRect.corner
                    val j = n.y.toInt() / 10
                    val i = n.x.toInt() / 10
                    threadling[j] = i
                }
            }
//            extend(ScreenRecorder())
            extend {
                drawer.clear(rgb(0.2))
                drawer.rectangles {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            if (tieUp[j][i]) {
                                fill = ColorRGBa.BLACK
                            } else {
                                fill = ColorRGBa.WHITE
                            }
                            rectangle(i * 10.0, j * 10.0, 10.0, 10.0)
                        }
                    }
                }

                drawer.rectangles {
                    for (j in 0 until width) {
                        for (i in 0 until warps) {
                            if (drafting[i] == j) {
                                fill = ColorRGBa.BLACK
                            } else {
                                fill = ColorRGBa.WHITE
                            }
                            rectangle(i * 10.0 + (width+1) * 10.0, j * 10.0, 10.0, 10.0)
                        }
                    }
                }

                drawer.rectangles {
                    for (j in 0 until wefts) {
                        for (i in 0 until width) {
                            if (threadling[j] == i ) {
                                fill = ColorRGBa.BLACK
                            } else {
                                fill = ColorRGBa.WHITE
                            }
                            rectangle(i * 10.0, j * 10.0 + (width+1) * 10.0, 10.0, 10.0)
                        }
                    }
                }

                drawer.rectangles {
                    stroke = null
                    for (j in 0 until wefts) {
                        for (i in 0 until warps) {

                            val d = drafting[i]
                            val t = threadling[j]

                            val c = s[i, j].r

                            var over = tieUp[d][t]
                            if (c > 0.8) {
                                over = !over
                            }

                            if (over) {
                                fill = ColorRGBa.BLACK
                                rectangle(i * 10.0 + (width+1) * 10.0, j * 10.0 + (width+1) * 10.0 + 1, 10.0, 8.0)
                            } else {
                                fill = ColorRGBa.WHITE
                                rectangle(i * 10.0 + (width+1) * 10.0 + 1.0, j * 10.0 + (width+1) * 10.0, 8.0, 10.0)
                            }


                        }
                    }
                }

                drawer.fill = ColorRGBa.RED
                //drawer.image(textImage)
            }
        }
    }
}