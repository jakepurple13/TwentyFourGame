import androidx.compose.ui.graphics.Color

enum class ThemeColor(
    val seedColor: Color,
) {
    Dynamic(Color.Transparent),
    Blue(Color.Blue),
    Red(Color.Red),
    Green(Color.Green),
    Yellow(Color.Yellow),
    Cyan(Color.Cyan),
    Magenta(Color.Magenta),
    Custom(Color.Transparent),
}