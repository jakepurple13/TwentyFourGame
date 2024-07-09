import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val UndoIcon: ImageVector
    get() {
        if (_undoIcon != null) {
            return _undoIcon!!
        }
        _undoIcon = Builder(
            name =
            "Undo24dpE8eaedFill0Wght400Grad0Opsz24", defaultWidth = 24.0.dp, defaultHeight =
            24.0.dp, viewportWidth = 960.0f, viewportHeight = 960.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFe8eaed)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(280.0f, 760.0f)
                verticalLineToRelative(-80.0f)
                horizontalLineToRelative(284.0f)
                quadToRelative(63.0f, 0.0f, 109.5f, -40.0f)
                reflectiveQuadTo(720.0f, 540.0f)
                quadToRelative(0.0f, -60.0f, -46.5f, -100.0f)
                reflectiveQuadTo(564.0f, 400.0f)
                lineTo(312.0f, 400.0f)
                lineToRelative(104.0f, 104.0f)
                lineToRelative(-56.0f, 56.0f)
                lineToRelative(-200.0f, -200.0f)
                lineToRelative(200.0f, -200.0f)
                lineToRelative(56.0f, 56.0f)
                lineToRelative(-104.0f, 104.0f)
                horizontalLineToRelative(252.0f)
                quadToRelative(97.0f, 0.0f, 166.5f, 63.0f)
                reflectiveQuadTo(800.0f, 540.0f)
                quadToRelative(0.0f, 94.0f, -69.5f, 157.0f)
                reflectiveQuadTo(564.0f, 760.0f)
                lineTo(280.0f, 760.0f)
                close()
            }
        }
            .build()
        return _undoIcon!!
    }

private var _undoIcon: ImageVector? = null
