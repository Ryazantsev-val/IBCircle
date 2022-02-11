import com.soywiz.klock.DateTime
import com.soywiz.klock.milliseconds
import com.soywiz.korge.Korge
import com.soywiz.korge.input.onDown
import com.soywiz.korge.tween.get
import com.soywiz.korge.tween.tween
import com.soywiz.korge.view.Circle
import com.soywiz.korge.view.xy
import com.soywiz.korim.color.Colors
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localCurrentDirVfs
import com.soywiz.korio.lang.format
import com.soywiz.korio.stream.AsyncStream
import com.soywiz.korio.stream.writeString
import com.soywiz.korma.geom.ScaleMode
import com.soywiz.korma.interpolation.Easing
import kotlin.math.hypot
import kotlin.random.Random

val BOARD_WIDTH = 500
val BOARD_HEIGHT = 400

suspend fun main() = Korge( scaleMode = ScaleMode.NO_SCALE, width = BOARD_WIDTH, height = BOARD_HEIGHT, title = "IBCircle", bgcolor = Colors.WHITE, clipBorders = true) {

    val attackCircle = Circle(25.0, Colors.RED)
    val protectCircle = Circle(25.0, Colors.GREEN)

    attackCircle.xy(Random.nextInt(0, BOARD_WIDTH), Random.nextInt(0, BOARD_HEIGHT))
    protectCircle.xy(Random.nextInt(0, BOARD_WIDTH), Random.nextInt(0, BOARD_HEIGHT))

    addChild(attackCircle)
    addChild(protectCircle)

    val logFile = createFile()

    for ((i, circle) in listOf(attackCircle, protectCircle).withIndex()) {
        launchImmediately {
            while (true) {
                tweenCircle(circle)
            }
        }
        circle.onDown {
            launchImmediately {
                val scale = circle.scale
                tween(
                    circle::scale[scale + 0.2],
                    time = 65.milliseconds,
                    easing = Easing.LINEAR
                )
                tween(
                    circle::scale[1.0],
                    time = 65.milliseconds,
                    easing = Easing.LINEAR
                )
            }

            writeLog(it.currentEvent!!.x, it.currentEvent!!.y, i, logFile)
        }
    }
}

val pathPairs = listOf(
    Pair(Easing.LINEAR, 0.3),
    Pair(Easing.EASE_IN_OUT_QUAD, 0.28),
)

suspend fun tweenCircle(circle: Circle) {
    val curX = circle.x
    val curY = circle.y

    val minDistance = 70.0

    val newX = maxOf(Random.nextInt(circle.radius.toInt(), BOARD_WIDTH-circle.radius.toInt()).toDouble(), minDistance)
    val newY = maxOf(Random.nextInt(circle.radius.toInt(), BOARD_HEIGHT-circle.radius.toInt()).toDouble(), maxOf(newX-curX-minDistance, 0.0))

    val distance = hypot(newX-curX, newY-curY)

    val pathPair = pathPairs.random()

    circle.tween(circle::x[newX], circle::y[newY], time = (distance/pathPair.second).toInt().milliseconds, easing = pathPair.first)
}


suspend fun writeLog(x: Int, y: Int, type: Int, logFile: AsyncStream) {
//    println("${getTimePrefix()} — ($x, $y) — ${if (type==0) "угроза" else "защита"}")
    logFile.writeString("${getTimePrefix()} — ($x, $y) — ${if (type==0) "угроза" else "защита"}\r\n")
}

fun getTimePrefix(): String {
    val dt = DateTime.nowLocal()
    return "${"%02d".format( dt.dayOfMonth+1)}.${"%02d".format( dt.month1)}.${dt.yearInt} ${"%02d".format(dt.hours)}:${"%02d".format(dt.minutes)}:${"%02d".format(dt.seconds)}.${"%03d".format( dt.milliseconds)}"
}

suspend fun createFile(): AsyncStream {
    val file = localCurrentDirVfs["IBLog_${getTimePrefix().replace('.', '_').replace(':', '_').replace(' ', '_')}.txt"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
    file.writeString("Program started. Let's protect SWSU from bad circle!\r\n")
    return file
}