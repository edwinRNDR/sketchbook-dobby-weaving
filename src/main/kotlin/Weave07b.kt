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
import org.openrndr.draw.BlendMode
import org.openrndr.draw.ColorType
import org.openrndr.draw.DepthTestPass
import org.openrndr.draw.TransformTarget
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolated
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.draw.renderTarget
import org.openrndr.drawImage
import org.openrndr.events.listen
import org.openrndr.extra.camera.Camera2DManual
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.extra.color.presets.VIOLET
import org.openrndr.extra.gui.WindowedGUI
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.ColorParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.parameters.TextParameter
import org.openrndr.extra.shapes.splines.catmullRom
import org.openrndr.extra.shapes.splines.toPath3D
import org.openrndr.extra.textwriter.writer
import org.openrndr.launch
import org.openrndr.math.Vector3
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.pow
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
            var warps = 250
            var wefts = 220
            var width = 8
            var vizMode = 0


            var tieUp0 = Array(width) { Array(width) { Double.uniform() < 0.5 } }
            // vertical
            var threadling0 = IntArray(wefts) { (it.mod(5) + it / 15).mod(width) }
            // horizontal
            var drafting0 = IntArray(warps) { (it.mod(5) + it / 15).mod(width) }


            var tieUp1 = Array(width) { Array(width) { Double.uniform() < 0.5 } }
            // vertical
            var threadling1 = IntArray(wefts) { (it.mod(5) + it / 15).mod(width) }
            // horizontal
            var drafting1 = IntArray(warps) { (it.mod(5) + it / 15).mod(width) }


            var tieUp2 = Array(width) { Array(width) { Double.uniform() < 0.5 } }
            // vertical
            var threadling2 = IntArray(wefts) { (it.mod(5) + it / 15).mod(width) }
            // horizontal
            var drafting2 = IntArray(warps) { (it.mod(5) + it / 15).mod(width) }


            var tieUp3 = Array(width) { Array(width) { Double.uniform() < 0.5 } }
            // vertical
            var threadling3 = IntArray(wefts) { (it.mod(5) + it / 15).mod(width) }
            // horizontal
            var drafting3 = IntArray(warps) { (it.mod(5) + it / 15).mod(width) }


            val tieUpRect = Rectangle(0.0, 0.0, width * 10.0, width * 10.0)
            val draftingRect = Rectangle((width + 1) * 10.0, 0.0, warps * 10.0, width * 10.0)
            val threadlingRect = Rectangle(0.0, (width + 1) * 10.0, width * 10.0, wefts * 10.0)


            fun waviness(x: Int, y: Int): Double {
                return 0.0
            }

            fun weftStrokeWeight(x: Int): Double {
                return 0.75 + 0.25 * when (vizMode) {
                    0 -> cos(x * 2 * PI / wefts) * 0.5 + 0.5
                    1 -> 1.0 - (cos(x * 2 * PI / wefts) * 0.5 + 0.5)
                    2 -> x.toDouble() / wefts.toDouble()
                    3 -> cos(x * 8 * PI / wefts) * 0.5 + 0.5
                    4 -> (abs(wefts / 2.0 - x) / (wefts / 2.0)) * 1.0
                    5 -> (1.0 - (abs(wefts / 2.0 - x) / (wefts / 2.0))) * 1.0
                    else -> 1.0
                }
            }

            fun warpStrokeWeight(x: Int): Double {
                return 0.75 + 0.25 * when (vizMode) {
                    0 -> cos(x * 2 * PI / warps) * 0.5 + 0.5
                    1 -> 1.0 - (cos(x * 2 * PI / warps) * 0.5 + 0.5)
                    2 -> x.toDouble() / warps.toDouble() * 7.0
                    3 -> sin(x * 8 * PI / warps) * 0.5 + 0.5
                    4 -> (abs(warps / 2.0 - x) / (warps / 2.0)) * 1.0
                    5 -> (1.0 - (abs(warps / 2.0 - x) / (warps / 2.0))) * 1.0
                    else -> 1.0
                }
            }

            fun isWeave0Over(x: Int, y: Int): Boolean {
                val di = drafting0[x]
                val ti = threadling0[y]
                return tieUp0[di][ti]
            }

            fun isWeave1Over(x: Int, y: Int): Boolean {
                val di = drafting1[x]
                val ti = threadling1[y]
                return tieUp1[di][ti]
            }

            fun isWeave2Over(x: Int, y: Int): Boolean {
                val di = drafting2[x]
                val ti = threadling2[y]
                return tieUp2[di][ti]
            }

            fun isWeave3Over(x: Int, y: Int): Boolean {
                val di = drafting3[x]
                val ti = threadling3[y]
                return tieUp3[di][ti]
            }


            fun isWeaveOver(x: Int, y: Int): Boolean {

                val mx = if (x <= 250) x else 250 - (x - 250)

                val bx = (mx / (250.0)).map(0.25, 0.75, 0.0, 1.0, clamp = true)
                val by = (y / (wefts.toDouble())).map(0.25, 0.75, 0.0, 1.0, clamp = true)

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

                return when (cx + cy * 2) {
                    0 -> isWeave0Over(x, y)
                    1 -> isWeave1Over(x, y)
                    2 -> isWeave2Over(x, y)
                    3 -> isWeave3Over(x, y)
                    else -> error("no such case")
                }


            }


            val gui = WindowedGUI()
            val weaveSettings = object {
                @DoubleParameter("weight", 0.0, 10.0)
                var weight = 5.0


                @ColorParameter("background color")
                var backgroundColor = ColorRGBa.WHITE

                @ColorParameter("warp color")
                var warpColor = ColorRGBa.GRAY

                @ColorParameter("weft color")
                var weftColor = ColorRGBa.BLACK

                @DoubleParameter("weft waviness", 0.0, 20.0)
                var weftWaviness = 0.0

                @DoubleParameter("warp waviness", 0.0, 20.0)
                var warpWaviness = 0.0

                @ActionParameter("load pattern BR")
                fun loadPattern0() {
                    openFileDialog { file ->
                        val json = file.readText()
                        val draft = Json.decodeFromString<Draft>(json)
                        tieUp0 = draft.tieUp
                        threadling0 = IntArray(wefts) { y -> draft.threadling[y.mod(draft.threadling.size)] }
                        drafting0 = IntArray(warps) { x -> draft.drafting[x.mod(draft.drafting.size)] }
                    }
                }

                @ActionParameter("load pattern BL")
                fun loadPattern1() {
                    openFileDialog { file ->
                        val json = file.readText()
                        val draft = Json.decodeFromString<Draft>(json)
                        tieUp1 = draft.tieUp
                        threadling1 = IntArray(wefts) { y -> draft.threadling[y.mod(draft.threadling.size)] }
                        drafting1 = IntArray(warps) { x -> draft.drafting[x.mod(draft.drafting.size)] }
                    }
                }

                @ActionParameter("load pattern TR")
                fun loadPattern2() {
                    openFileDialog { file ->
                        val json = file.readText()
                        val draft = Json.decodeFromString<Draft>(json)
                        tieUp2 = draft.tieUp
                        threadling2 = IntArray(wefts) { y -> draft.threadling[y.mod(draft.threadling.size)] }
                        drafting2 = IntArray(warps) { x -> draft.drafting[x.mod(draft.drafting.size)] }
                    }
                }

                @ActionParameter("load pattern TL")
                fun loadPattern3() {
                    openFileDialog { file ->
                        val json = file.readText()
                        val draft = Json.decodeFromString<Draft>(json)
                        tieUp3 = draft.tieUp
                        threadling3 = IntArray(wefts) { y -> draft.threadling[y.mod(draft.threadling.size)] }
                        drafting3 = IntArray(warps) { x -> draft.drafting[x.mod(draft.drafting.size)] }
                    }
                }
            }


            val typeSettings = object {
                @BooleanParameter("draw accent")
                var drawAccent = true

                @DoubleParameter("text width", 10.0, 2560.0)
                var textWidth = 2560.0

                @DoubleParameter("letter waviness", -20.0, 20.0)
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
            val textImage = drawImage(250, 220, contentScale = 1.0) {
                drawer.clear(ColorRGBa.BLACK)
            }//loadImage("data/images/survey-1209-v1.png")


            val s = textImage.shadow
            s.download()

            fun drawWeave() {
                drawer.isolated {

                    drawer.drawStyle.depthWrite = true
                    drawer.drawStyle.depthTestPass = DepthTestPass.LESS_OR_EQUAL
                    drawer.stroke = weaveSettings.warpColor


                    for (x in 0 until warps) {
                        drawer.shadeStyle?.parameter("line", x.toDouble() / warps)
                        drawer.strokeWeight = weaveSettings.weight * warpStrokeWeight(x)

                        val path = (0 until wefts).map { y ->
                            var waviness = weaveSettings.warpWaviness
                            val over = isWeaveOver(x, y)
                            val c = s[x, y].r
                            //waviness += c * typeSettings.letterWaviness
                            val o = if (over) 1 else 0
                            Vector3(x * 10.0 - o * waviness, y * 10.0, 0.0)
                        }.catmullRom(0.2, false).toPath3D()

                        drawer.path(path)
//                            drawer.path(
//                                LineSegment3D(
//                                    Vector3(x * 10.0, 0.0, 0.0),
//                                    Vector3(x * 10.0, wefts * 10.0, 0.0)
//                                ).path
//                            )
                    }
                    drawer.stroke = typeSettings.accentColor
                    if (typeSettings.drawAccent)
                        drawer.isolated {
                            drawer.drawStyle.depthWrite = false
                            for (y in 0 until wefts) {
                                drawer.strokeWeight = weaveSettings.weight * weftStrokeWeight(y)

                                val path = (0 until warps).map { x ->
                                    var waviness = weaveSettings.weftWaviness
                                    //0.0 // (cos(seconds * 0.25 * 2 * PI + x * 0.1 + y * 0.1) * 0.5 + 0.5) * 7.0
                                    val over = isWeaveOver(x, y)
                                    val dx = warps / 2 - x
                                    val dy = wefts / 2 - y
                                    val d = sqrt(dx * dx + dy * dy * 1.0)
                                    val k = sin(seconds * (1.0 / 16.0) * PI * 2.0) * 60.0 + 60.0
                                    val sd = 1.0// smoothstep(k + 0.0, -60 + k, d)
                                    val c = s[x, y].r
                                    waviness += c * typeSettings.letterWaviness * typeSettings.letterAccent

                                    var o = if (over) 1 else 0

                                    o = 1 - o
                                    if (c > 0.5) {
                                        o = 1
                                    }


                                    Vector3(x * 10.0, y * 10.0 - o * waviness, o * 1.0 - 0.5)

                                }.catmullRom(0.0001, false).toPath3D()

                                drawer.path(path)
                            }
                        }
                    drawer.stroke = weaveSettings.weftColor
                    for (y in 0 until wefts) {
                        drawer.strokeWeight = weaveSettings.weight * weftStrokeWeight(y)
                        //drawer.strokeWeight = 4.0 //cos(seconds * 0.25 * PI + y * 0.1) * 2.0 + 3.0

                        val path = (0 until warps).map { x ->
                            var waviness = weaveSettings.weftWaviness
                            val over = isWeaveOver(x, y)
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


            val renderSettings = object {
                @ActionParameter("render")
                fun saveToFile() {
                    saveFileDialog(supportedExtensions = listOf("Raster images" to listOf("png"))) {

                        launch {

                            val tileSize = 128
                            val rows = ceil(wefts / 128.0).toInt()
                            val cols = ceil(warps / 128.0).toInt()


                            for (row in 0 until rows) {
                                for (col in 0 until cols) {
                                    val rt = renderTarget(
                                        tileSize * 10,
                                        tileSize * 10,
                                        contentScale = 12.0

                                    ) {
                                        colorBuffer()
                                        depthBuffer()
                                    }
                                    val blendRt = renderTarget(rt.width, rt.height, contentScale = rt.contentScale) {
                                        colorBuffer(type = ColorType.FLOAT32)
                                    }

                                    drawer.isolatedWithTarget(blendRt) {
                                        drawer.clear(ColorRGBa.BLACK)
                                    }

                                    for (j in -2..2)
                                        for (i in -2..2) {
                                            drawer.isolatedWithTarget(rt) {
                                                drawer.clear(weaveSettings.backgroundColor)
                                                drawer.translate(i / 48.0, j / 48.0, 0.0, TransformTarget.VIEW)
                                                drawer.translate(-col * tileSize * 10.0, -row * tileSize * 10.0, 0.0, TransformTarget.VIEW)
                                                drawer.ortho(rt)
                                                drawWeave()
                                            }
                                            drawer.isolatedWithTarget(blendRt) {
                                                drawer.drawStyle.blendMode = BlendMode.ADD
                                                drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(1 / (25.0)))

                                                drawer.ortho(blendRt)
                                                drawer.image(rt.colorBuffer(0))
                                            }

                                        }

                                    val resolved = colorBuffer(rt.width, rt.height, contentScale = rt.contentScale)
                                    blendRt.colorBuffer(0).copyTo(resolved)

                                    val tileFile = File(
                                        it.parentFile,
                                        "${it.nameWithoutExtension}-${
                                            String.format(
                                                "%02d",
                                                row
                                            )
                                        }-${String.format("%02d", col)}.png"
                                    )
                                    resolved.saveToFile(tileFile, async = false)
                                    rt.destroy()
                                    blendRt.destroy()
                                }

                            }
                        }

                    }

                }
            }

            gui.add(renderSettings, "Render")
            gui.add(typeSettings, "Typography")
            gui.add(weaveSettings, "Weave")

            extend(gui) {}




            mouse.dragged.listen {
                if (mouse.position in tieUpRect) {
                    val j = mouse.position.y.toInt() / 10
                    val i = mouse.position.x.toInt() / 10
                    tieUp0[j][i] = if (it.modifiers.contains(KeyModifier.ALT)) false else true
                }

                if (mouse.position in draftingRect) {
                    val m = mouse.position - draftingRect.corner
                    val i = m.x.toInt() / 10
                    val j = m.y.toInt() / 10
                    drafting0[i] = j
                }

                if (mouse.position in threadlingRect) {
                    val n = mouse.position - threadlingRect.corner
                    val j = n.y.toInt() / 10
                    val i = n.x.toInt() / 10
                    threadling0[j] = i
                }

            }

            listOf(mouse.buttonUp).listen {
                if (mouse.position in tieUpRect) {
                    val j = mouse.position.y.toInt() / 10
                    val i = mouse.position.x.toInt() / 10
                    tieUp0[j][i] = !tieUp0[j][i]
                    it.cancelPropagation()
                }

                if (mouse.position in draftingRect) {
                    val m = mouse.position - draftingRect.corner
                    val i = m.x.toInt() / 10
                    val j = m.y.toInt() / 10
                    drafting0[i] = j
                    it.cancelPropagation()
                }

                if (mouse.position in threadlingRect) {
                    val n = mouse.position - threadlingRect.corner
                    val j = n.y.toInt() / 10
                    val i = n.x.toInt() / 10
                    threadling0[j] = i
                    it.cancelPropagation()
                }
            }

            val camera = Camera2DManual()
            keyboard.keyDown.listen {
                if (it.modifiers.isEmpty()) {
                    when (it.key) {
                        KEY_PAGE_UP -> {
                            val copy = tieUp0.map { it.map { it } }
                            for (j in 0 until width) {
                                for (i in 0 until width) {
                                    tieUp0[j][i] = copy[(j + 1).mod(width)][i]
                                }
                            }
                        }

                        KEY_PAGE_DOWN -> {
                            val copy = tieUp0.map { it.map { it } }
                            for (j in 0 until width) {
                                for (i in 0 until width) {
                                    tieUp0[j][i] = copy[(j - 1).mod(width)][i]
                                }
                            }
                        }

                        KEY_HOME -> {
                            val copy = tieUp0.map { it.map { it && it } }
                            for (j in 0 until width) {
                                for (i in 0 until width) {
                                    tieUp0[j][i] = copy[j][(i + 1).mod(width)]
                                }
                            }
                        }

                        KEY_END -> {
                            val copy = tieUp0.map { it.map { it && it } }
                            for (j in 0 until width) {
                                for (i in 0 until width) {
                                    tieUp0[j][i] = copy[j][(i - 1).mod(width)]
                                }
                            }
                        }

                        KEY_ARROW_DOWN -> {
                            for (i in drafting0.indices) {
                                drafting0[i] = (drafting0[i] + 1).mod(width)
                            }
                        }

                        KEY_ARROW_UP -> {
                            for (i in drafting0.indices) {
                                drafting0[i] = (drafting0[i] - 1).mod(width)
                            }
                        }

                        KEY_ARROW_RIGHT -> {
                            for (i in threadling0.indices) {
                                threadling0[i] = (threadling0[i] + 1).mod(width)
                            }
                        }

                        KEY_ARROW_LEFT -> {
                            for (i in threadling0.indices) {
                                threadling0[i] = (threadling0[i] - 1).mod(width)
                            }
                        }

                    }
                } else if (it.modifiers.contains(KeyModifier.SHIFT)) {
                    when (it.key) {
                        KEY_ARROW_RIGHT -> {
                            val copy = drafting0.clone()
                            for (i in drafting0.indices) {
                                drafting0[i] = copy[(i - 1).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_LEFT -> {
                            val copy = drafting0.clone()
                            for (i in drafting0.indices) {
                                drafting0[i] = copy[(i + 1).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_UP -> {
                            val copy = threadling0.clone()
                            for (i in threadling0.indices) {
                                threadling0[i] = copy[(i + 1).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_DOWN -> {
                            val copy = threadling0.clone()
                            for (i in threadling0.indices) {
                                threadling0[i] = copy[(i - 1).mod(copy.size)]
                            }
                        }
                    }
                } else if (it.modifiers.contains(KeyModifier.ALT)) {
                    when (it.key) {
                        KEY_ARROW_RIGHT -> {
                            val copy = drafting0.clone()
                            for (i in drafting0.indices) {
                                drafting0[i] = copy[(i / 2).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_LEFT -> {
                            val copy = drafting0.clone()
                            for (i in drafting0.indices) {
                                drafting0[i] = copy[(i * 2).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_UP -> {
                            val copy = threadling0.clone()
                            for (i in threadling0.indices) {
                                threadling0[i] = copy[(i / 2).mod(copy.size)]
                            }
                        }

                        KEY_ARROW_DOWN -> {
                            val copy = threadling0.clone()
                            for (i in threadling0.indices) {
                                threadling0[i] = copy[(i * 2).mod(copy.size)]
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
                        file.writeText(Json.encodeToString(Draft(width, warps, wefts, tieUp0, drafting0, threadling0)))
                    }
                }
                if (c == 'x') {
                    openFileDialog { file ->
                        val json = file.readText()
                        val draft = Json.decodeFromString<Draft>(json)
                        tieUp0 = draft.tieUp
                        threadling0 = IntArray(wefts) { y -> draft.threadling[y.mod(draft.threadling.size)] }
                        drafting0 = IntArray(warps) { x -> draft.drafting[x.mod(draft.drafting.size)] }
                    }
                }
                if (c == 'b') {
                    openFileDialog { file ->
                        val json = file.readText()
                        val draft = Json.decodeFromString<Draft>(json)
                        tieUp1 = draft.tieUp
                        threadling1 = IntArray(wefts) { y -> draft.threadling[y.mod(draft.threadling.size)] }
                        drafting1 = IntArray(warps) { x -> draft.drafting[x.mod(draft.drafting.size)] }
                    }
                }
                if (c == 'n') {
                    openFileDialog { file ->
                        val json = file.readText()
                        val draft = Json.decodeFromString<Draft>(json)
                        tieUp2 = draft.tieUp
                        threadling2 = IntArray(wefts) { y -> draft.threadling[y.mod(draft.threadling.size)] }
                        drafting2 = IntArray(warps) { x -> draft.drafting[x.mod(draft.drafting.size)] }
                    }
                }
                if (c == 'm') {
                    openFileDialog { file ->
                        val json = file.readText()
                        val draft = Json.decodeFromString<Draft>(json)
                        tieUp3 = draft.tieUp
                        threadling3 = IntArray(wefts) { y -> draft.threadling[y.mod(draft.threadling.size)] }
                        drafting3 = IntArray(warps) { x -> draft.drafting[x.mod(draft.drafting.size)] }
                    }
                }

                if (c == 'q') {

                    for (i in drafting0.indices) {
                        drafting0[i] = drafting0[i] * factor + wtriangle(i, width)
                    }
                }
                if (c == 'w') {
                    for (i in drafting0.indices) {
                        drafting0[i] = drafting0[i] * factor + wsaw(i, width)
                    }
                }

                if (c == 'e') {
                    for (i in drafting0.indices) {
                        drafting0[i] = drafting0[i] * factor + wstairs(i, 3)
                    }
                }

                if (c == 'r') {
                    for (i in drafting0.indices) {
                        drafting0[i] = drafting0[i] * factor + wstairs(i, 4)
                    }
                }
                if (c == 't') {
                    for (i in drafting0.indices) {
                        drafting0[i] =
                            drafting0[i] * factor + (cos(i * 1 * PI / width) * (width) / 2.0 + (width) / 2).toInt()
                    }
                }


                if (c == 'a') {
                    for (i in threadling0.indices) {
                        threadling0[i] = threadling0[i] * factor + wtriangle(i, width)
                    }
                }
                if (c == 's') {
                    for (i in threadling0.indices) {
                        threadling0[i] = threadling0[i] * factor + wsaw(i, width)
                    }
                }
                if (c == 'd') {
                    for (i in threadling0.indices) {
                        threadling0[i] = threadling0[i] * factor + wstairs(i, 3)
                    }
                }
                if (c == 'f') {
                    for (i in threadling0.indices) {
                        threadling0[i] = threadling0[i] * factor + wstairs(i, 4)
                    }
                }
                if (c == 'g') {
                    for (i in threadling0.indices) {
                        threadling0[i] =
                            threadling0[i] * factor + (cos(i * 1 * PI / width) * (width) / 2.0 + (width) / 2).toInt()
                    }
                }

                if (c == '0') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            tieUp0[j][i] = false
                        }
                    }
                }

                if (c == '1') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            tieUp0[j][i] = !tieUp0[j][i]
                        }
                    }
                }

                if (c == '2') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            tieUp0[j][i] = tieUp0[j][i] xor (Double.uniform() < 0.5)
                        }
                    }
                }

                if (c == '3') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            val dj = j - (width - 1) / 2.0
                            val di = i - (width - 1) / 2.0
                            val d = sqrt(dj * dj + di * di)
                            tieUp0[j][i] = tieUp0[j][i] xor (d < 2.0)
                        }
                    }
                }
                if (c == '4') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            val dj = j - (width - 1) / 2.0
                            val di = i - (width - 1) / 2.0
                            val d = sqrt(dj * dj + di * di)
                            tieUp0[j][i] = tieUp0[j][i] xor (d < 3.0)
                        }
                    }
                }
                if (c == '5') {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            val dj = j - (width - 1) / 2.0
                            val di = i - (width - 1) / 2.0
                            val d = sqrt(dj * dj + di * di)
                            tieUp0[j][i] = tieUp0[j][i] xor (d < 4.0)
                        }
                    }
                }
                if (c == '6') {
                    val copy = tieUp0.clone()
                    for (j in 0 until width) {
                        for (i in 0 until width) {

                            tieUp0[j][i] = tieUp0[j][i] xor copy[i][j]
                        }
                    }
                }

                if (c == '7') {
                    val copy = tieUp0.map { it.map { it && it } }
                    for (j in 0 until width) {
                        for (i in 0 until width) {

                            tieUp0[j][i] = copy[i][j]
                        }
                    }
                }

                if (c == '8') {
                    val copy = tieUp0.map { it.map { it && it } }
                    for (j in 0 until width) {
                        for (i in 0 until width) {

                            tieUp0[j][i] = (j xor i).mod(5) == 1
                        }
                    }
                }
                if (c == '9') {
                    val copy = tieUp0.map { it.map { it && it } }
                    for (j in 0 until width) {
                        for (i in 0 until width) {

                            tieUp0[j][i] = (j xor i).mod(3) == 2
                        }
                    }
                }

                for (i in threadling0.indices) {
                    threadling0[i] = threadling0[i].mod(width)
                }

                for (i in drafting0.indices) {
                    drafting0[i] = drafting0[i].mod(width)
                }
            }


//            extend(ScreenRecorder())
            extend {


                s.download()

                drawer.clear(weaveSettings.backgroundColor)
                drawer.rectangles {
                    for (j in 0 until width) {
                        for (i in 0 until width) {
                            if (tieUp0[j][i]) {
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
                            if (drafting0[i] == j) {
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
                            if (threadling0[j] == i) {
                                fill = ColorRGBa.BLACK
                            } else {
                                fill = ColorRGBa.WHITE
                            }
                            rectangle(i * 10.0, j * 10.0 + (width + 1) * 10.0, 10.0, 10.0)
                        }
                    }
                }


                camera.isolated {
                    drawer.translate(10.0 * (width + 1), 10.0 * (width + 1))
                    drawWeave()
                }
            }
        }
    }
}