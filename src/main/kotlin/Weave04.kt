import kotlinx.serialization.json.Json
import lib.Draft
import lib.wsaw
import lib.wstairs
import lib.wtriangle
import org.openrndr.KEY_ARROW_DOWN
import org.openrndr.KEY_ARROW_LEFT
import org.openrndr.KEY_ARROW_RIGHT
import org.openrndr.KEY_ARROW_UP
import org.openrndr.KEY_END
import org.openrndr.KEY_HOME
import org.openrndr.KEY_PAGE_DOWN
import org.openrndr.KEY_PAGE_UP
import org.openrndr.KeyModifier
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.openFileDialog
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.DepthTestPass
import org.openrndr.draw.isolated
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadFont
import org.openrndr.draw.renderTarget
import org.openrndr.drawImage
import org.openrndr.events.listen
import org.openrndr.extra.color.presets.VIOLET
import org.openrndr.extra.gui.WindowedGUI
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.parameters.ColorParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.parameters.TextParameter
import org.openrndr.extra.shapes.splines.catmullRom
import org.openrndr.extra.shapes.splines.toPath3D
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector3
import org.openrndr.shape.Rectangle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun main() {
    application {
        configure {
            width = 1800
            height = 1100
            windowResizable = true
            multisample = WindowMultisample.SampleCount(8)
        }
        program {

            val gui = WindowedGUI()

            val weaveSettings = object {
                @ColorParameter("background color")
                var backgroundColor = ColorRGBa.WHITE

                @ColorParameter("warp color")
                var warpColor = ColorRGBa.GRAY

                @ColorParameter("weft color")
                var weftColor = ColorRGBa.BLACK

                @DoubleParameter("weft waviness", 0.0, 20.0)
                var weftWaviness = 0.0
            }


            val typeSettings = object {
                @DoubleParameter("letter waviness", 0.0, 20.0)
                var letterWaviness = 7.0

                @DoubleParameter("letter accent", 0.0, 2.0)
                var letterAccent = 0.5

                @ColorParameter("accent color")
                var accentColor = ColorRGBa.VIOLET

                @TextParameter("text")
                var text = "Climate change"

                @DoubleParameter("leading", -40.0, 40.0)
                var leading = 0.0

                @DoubleParameter("spacing", -40.0, 40.0)
                var spacing = 0.0

                @IntParameter("size", 8, 64)
                var size = 48


            }
            gui.add(typeSettings, "Typography")
            gui.add(weaveSettings, "Weave")

            extend(gui) {}

            val textImage = drawImage(180, 100, contentScale = 1.0) {
                drawer.clear(ColorRGBa.BLACK)
                drawer.fontMap = loadFont("data/fonts/Roboto-Regular.ttf", 48.0)

                drawer.writer {
                    box = drawer.bounds
                    verticalAlign = 0.5
                    horizontalAlign = 0.5
                    text(typeSettings.text)
                }
            }

            var vizMode = 0

            val s = textImage.shadow
            s.download()
            var warps = 180
            var wefts = 100
            var width = 8

            var tieUp = Array(width) { Array(width) { false } }

            // vertical
            var threadling = IntArray(wefts) { (it.mod(5) + it / 15).mod(width) }
            // horizontal
            var drafting = IntArray(warps) { (it.mod(5) + it / 15).mod(width) }


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

            keyboard.keyDown.listen {
                if (it.modifiers.isEmpty()) {
                    when (it.key) {
                        KEY_PAGE_UP -> {
                            val copy = tieUp.map { it.map { it} }
                            for (j in 0 until width) {
                                for (i in 0 until width) {
                                    tieUp[j][i] = copy[(j+1).mod(width)][i]
                                }
                            }
                        }
                        KEY_PAGE_DOWN -> {
                            val copy = tieUp.map { it.map { it} }
                            for (j in 0 until width) {
                                for (i in 0 until width) {
                                    tieUp[j][i] = copy[(j-1).mod(width)][i]
                                }
                            }
                        }

                        KEY_HOME -> {
                            val copy = tieUp.map { it.map { it && it} }
                            for (j in 0 until width) {
                                for (i in 0 until width) {
                                    tieUp[j][i] = copy[j][(i+1).mod(width)]
                                }
                            }
                        }
                        KEY_END -> {
                            val copy = tieUp.map { it.map { it && it } }
                            for (j in 0 until width) {
                                for (i in 0 until width) {
                                    tieUp[j][i] = copy[j][(i-1).mod(width)]
                                }
                            }
                        }
                        KEY_ARROW_DOWN -> {
                            for (i in drafting.indices) {
                                drafting[i] = (drafting[i] + 1).mod(width)
                            }
                        }

                        KEY_ARROW_UP -> {
                            for (i in drafting.indices) {
                                drafting[i] = (drafting[i] - 1).mod(width)
                            }
                        }

                        KEY_ARROW_RIGHT -> {
                            for (i in threadling.indices) {
                                threadling[i] = (threadling[i] + 1).mod(width)
                            }
                        }

                        KEY_ARROW_LEFT -> {
                            for (i in threadling.indices) {
                                threadling[i] = (threadling[i] - 1).mod(width)
                            }
                        }

                    }
                } else if (it.modifiers.contains(KeyModifier.SHIFT)) {
                    when (it.key) {
                        KEY_ARROW_RIGHT -> {
                            val copy = drafting.clone()
                            for (i in drafting.indices) {
                                drafting[i] = copy[(i - 1).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_LEFT -> {
                            val copy = drafting.clone()
                            for (i in drafting.indices) {
                                drafting[i] = copy[(i + 1).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_UP -> {
                            val copy = threadling.clone()
                            for (i in threadling.indices) {
                                threadling[i] = copy[(i + 1).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_DOWN -> {
                            val copy = threadling.clone()
                            for (i in threadling.indices) {
                                threadling[i] = copy[(i - 1).mod(copy.size)]
                            }
                        }
                    }
                } else if (it.modifiers.contains(KeyModifier.ALT)) {
                    when (it.key) {
                        KEY_ARROW_RIGHT -> {
                            val copy = drafting.clone()
                            for (i in drafting.indices) {
                                drafting[i] = copy[(i / 2).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_LEFT -> {
                            val copy = drafting.clone()
                            for (i in drafting.indices) {
                                drafting[i] = copy[(i * 2).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_UP -> {
                            val copy = threadling.clone()
                            for (i in threadling.indices) {
                                threadling[i] = copy[(i / 2).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_DOWN -> {
                            val copy = threadling.clone()
                            for (i in threadling.indices) {
                                threadling[i] = copy[(i * 2).mod(copy.size)]
                            }
                        }
                    }
                }
            }
            keyboard.character.listen {
                val c = it.character.lowercase().first()

                val factor = if (it.character.isUpperCase()) {
                    1
                } else 0

                if (c == 'v') {
                    vizMode = (vizMode + 1).mod(10)
                }

                if (c == 'c') {
                    vizMode = (vizMode - 1).mod(10)
                }

                if (c == 'z') {
                    saveFileDialog { file ->
                        file.writeText(Json.encodeToString(Draft(width, warps, wefts, tieUp, drafting, threadling)))
                    }
                }
                if (c == 'x') {
                    openFileDialog { file ->
                        val json = file.readText()
                        val draft = Json.decodeFromString<Draft>(json)
                        tieUp = draft.tieUp
                        threadling = draft.threadling
                        drafting = draft.drafting
                        width = draft.width
                        warps = draft.warps
                        wefts = draft.wefts
                    }
                }

                if (c == 'q') {

                    for (i in drafting.indices) {
                        drafting[i] = drafting[i] * factor + wtriangle(i, width)
                    }
                }
                if (c == 'w') {
                    for (i in drafting.indices) {
                        drafting[i] = drafting[i] * factor + wsaw(i, width)
                    }
                }

                if (c == 'e') {
                    for (i in drafting.indices) {
                        drafting[i] = drafting[i] * factor + wstairs(i, 3)
                    }
                }

                if (c == 'r') {
                    for (i in drafting.indices) {
                        drafting[i] = drafting[i] * factor + wstairs(i, 4)
                    }
                }
                if (c == 't') {
                    for (i in drafting.indices) {
                        drafting[i] = drafting[i] * factor + (cos(i * 1*PI/width) * (width)/2.0 + (width)/2).toInt()
                    }
                }


                if (c == 'a') {
                    for (i in threadling.indices) {
                        threadling[i] = threadling[i] * factor + wtriangle(i, width)
                    }
                }
                if (c == 's') {
                    for (i in threadling.indices) {
                        threadling[i] = threadling[i] * factor + wsaw(i, width)
                    }
                }
                if (c == 'd') {
                    for (i in threadling.indices) {
                        threadling[i] = threadling[i] * factor + wstairs(i, 3)
                    }
                }
                if (c == 'f') {
                    for (i in threadling.indices) {
                        threadling[i] = threadling[i] * factor + wstairs(i, 4)
                    }
                }
                if (c == 'g') {
                    for (i in threadling.indices) {
                        threadling[i] = threadling[i] * factor + (cos(i * 1*PI/width) * (width)/2.0 + (width)/2).toInt()
                    }
                }

                if (c == '0') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            tieUp[j][i] = false
                        }
                    }
                }

                if (c == '1') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            tieUp[j][i] = !tieUp[j][i]
                        }
                    }
                }

                if (c == '2') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            tieUp[j][i] = tieUp[j][i] xor (Double.uniform() < 0.5)
                        }
                    }
                }

                if (c == '3') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            val dj = j - (width - 1) / 2.0
                            val di = i - (width - 1) / 2.0
                            val d = sqrt(dj * dj + di * di)
                            tieUp[j][i] = tieUp[j][i] xor (d < 2.0)
                        }
                    }
                }
                if (c == '4') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            val dj = j - (width - 1) / 2.0
                            val di = i - (width - 1) / 2.0
                            val d = sqrt(dj * dj + di * di)
                            tieUp[j][i] = tieUp[j][i] xor (d < 3.0)
                        }
                    }
                }
                if (c == '5') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            val dj = j - (width - 1) / 2.0
                            val di = i - (width - 1) / 2.0
                            val d = sqrt(dj * dj + di * di)
                            tieUp[j][i] = tieUp[j][i] xor (d < 4.0)
                        }
                    }
                }
                if (c == '6') {
                    val copy = tieUp.clone()
                    for (j in 0 until width) {
                        for (i in 0 until width) {

                            tieUp[j][i] = tieUp[j][i] xor copy[i][j]
                        }
                    }
                }

                if (c == '7') {
                    val copy = tieUp.map { it.map { it &&it} }
                    for (j in 0 until width) {
                        for (i in 0 until width) {

                            tieUp[j][i] = copy[i][j]
                        }
                    }
                }

                if (c == '8') {
                    val copy = tieUp.map { it.map { it &&it} }
                    for (j in 0 until width) {
                        for (i in 0 until width) {

                            tieUp[j][i] = (j xor i).mod(5) == 1
                        }
                    }
                }
                if (c == '9') {
                    val copy = tieUp.map { it.map { it &&it} }
                    for (j in 0 until width) {
                        for (i in 0 until width) {

                            tieUp[j][i] = (j xor i).mod(3) == 2
                        }
                    }
                }

                for (i in threadling.indices) {
                    threadling[i] = threadling[i].mod(width)
                }

                for (i in drafting.indices) {
                    drafting[i] = drafting[i].mod(width)
                }
            }
//            extend(ScreenRecorder())
            extend {

                val rt = renderTarget(textImage.width, textImage.height) {
                    colorBuffer(textImage)
                }

                drawer.isolatedWithTarget(rt) {
                    drawer.ortho(rt)
                    drawer.clear(ColorRGBa.BLACK)
                    drawer.fontMap = loadFont("data/fonts/Roboto-Regular.ttf", typeSettings.size.toDouble())


                    drawer.writer {
                        box = drawer.bounds
                        verticalAlign = 0.5
                        horizontalAlign = 0.5
                        leading = typeSettings.leading
                        tracking = typeSettings.spacing
                        if (typeSettings.text.length > 0) {
                            text(typeSettings.text)
                        }
                    }
                }
                s.download()

                drawer.clear(weaveSettings.backgroundColor)
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
                            rectangle(i * 10.0 + (width + 1) * 10.0, j * 10.0, 10.0, 10.0)
                        }
                    }
                }

                drawer.rectangles {
                    for (j in 0 until wefts) {
                        for (i in 0 until width) {
                            if (threadling[j] == i) {
                                fill = ColorRGBa.BLACK
                            } else {
                                fill = ColorRGBa.WHITE
                            }
                            rectangle(i * 10.0, j * 10.0 + (width + 1) * 10.0, 10.0, 10.0)
                        }
                    }
                }

                fun waviness(x: Int, y: Int): Double {
                    return 0.0
                }

                fun weftStrokeWeight(x: Int): Double {
                    return when(vizMode) {
                        0 -> cos(x * 2 * PI / wefts) * 3.5 + 3.5
                        1 -> cos(x * 4 * PI / wefts) * 3.5 + 3.5
                        2 -> x.toDouble() / wefts.toDouble() * 7.0
                        3 -> cos(x * 8 * PI / wefts) * 3.5 + 3.5
                        4 -> (abs(wefts/2.0 - x) / (wefts/2.0)) * 7.0
                        5 -> (1.0 - (abs(wefts/2.0 - x) / (wefts/2.0))) * 7.0
                        else -> 6.0
                    }
                }

                fun warpStrokeWeight(x: Int): Double {
                    return when(vizMode) {
                        0 ->  cos(x * 2 * PI / warps) * 3.5 + 3.5
                        1 ->  cos(x * 4 * PI / warps) * 3.5 + 3.5
                        2 -> x.toDouble() / warps.toDouble() * 7.0
                        3 -> sin(x * 8 * PI / warps) * 3.5 + 3.5
                        4 -> (abs(warps/2.0 - x) / (warps/2.0)) * 7.0
                        5 -> (1.0 - (abs(warps/2.0 - x) / (warps/2.0))) * 7.0
                        else -> 6.0
                    }
                }

                drawer.isolated {
                    drawer.translate(10.0 * (width + 1), 10.0 * (width + 1))
                    drawer.drawStyle.depthWrite = true
                    drawer.drawStyle.depthTestPass = DepthTestPass.LESS_OR_EQUAL
                    drawer.stroke = weaveSettings.warpColor
                    for (x in 0 until warps) {
                        drawer.strokeWeight = warpStrokeWeight(x)
                        drawer.lineSegment(Vector3(x * 10.0, 0.0, 0.0), Vector3(x * 10.0, wefts * 10.0, 0.0))
                    }

                    drawer.stroke = typeSettings.accentColor
                    for (y in 0 until wefts) {
                        drawer.strokeWeight = weftStrokeWeight(y)

                        val path = (0 until warps).map { x ->
                            var waviness = weaveSettings.weftWaviness
                            //0.0 // (cos(seconds * 0.25 * 2 * PI + x * 0.1 + y * 0.1) * 0.5 + 0.5) * 7.0
                            val di = drafting[x]
                            val ti = threadling[y]
                            var over = tieUp[di][ti]
                            val dx = warps / 2 - x
                            val dy = wefts / 2 - y
                            val d = sqrt(dx * dx + dy * dy * 1.0)
                            val k = sin(seconds * (1.0 / 16.0) * PI * 2.0) * 60.0 + 60.0
                            val sd = 1.0// smoothstep(k + 0.0, -60 + k, d)
                            val c = s[x, y].r
                            waviness += c * typeSettings.letterWaviness * 0.5

                            var o = if (over) 1 else 0

                            o = 1 - o
                            if (c > 0.5) {
                                o = 1
                            }


                            Vector3(x * 10.0, y * 10.0 - o * waviness, o * 1.0 - 0.5)

                        }.catmullRom(0.0001, false).toPath3D()

                        drawer.path(path)
                    }

                    drawer.stroke = weaveSettings.weftColor
                    for (y in 0 until wefts) {
                        drawer.strokeWeight = weftStrokeWeight(y)
                        //drawer.strokeWeight = 4.0 //cos(seconds * 0.25 * PI + y * 0.1) * 2.0 + 3.0

                        val path = (0 until warps).map { x ->
                            var waviness = weaveSettings.weftWaviness
                            val di = drafting[x]
                            val ti = threadling[y]
                            var over = tieUp[di][ti]
                            val dx = warps / 2 - x
                            val dy = wefts / 2 - y
                            val d = sqrt(dx * dx + dy * dy * 1.0)
                            val k = sin(seconds * (1.0 / 16.0) * PI * 2.0) * 60.0 + 60.0
                            val sd = 1.0// smoothstep(k + 0.0, -60 + k, d)
                            val c = s[x, y].r
                            waviness += c * typeSettings.letterWaviness

                            var o = if (over) 1 else 0

                            o = 1 - o
                            if (c > 0.5) {
                                o = 1
                            }
                            Vector3(x * 10.0, y * 10.0 - o * waviness, o * 1.0 - 0.4)
                        }.catmullRom(0.2, false).toPath3D()

                        drawer.path(path)
                    }
                }
            }
        }
    }
}