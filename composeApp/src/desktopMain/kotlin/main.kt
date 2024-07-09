import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.util.*

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "TwentyFourGame",
    ) {
        MaterialTheme(
            if(isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        ) {
            App()
        }
    }
}

fun main2() {
    val r = Random()
    val n = IntArray(N_CARDS)
    for (j in 0..9) {
        for (i in 0 until N_CARDS) {
            n[i] = 1 + r.nextInt(MAX_DIGIT)
            print(" ${n[i]}")
        }
        print(":  ")
        val stringBuilder = StringBuilder()
        print(if (solve24(n, stringBuilder)) "\t|\t" else "No solution")
        println(stringBuilder.toString())
    }
}