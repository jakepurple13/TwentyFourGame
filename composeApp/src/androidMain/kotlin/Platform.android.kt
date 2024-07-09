import android.os.Build
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.Modifier

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun buttonSize(): Modifier = Modifier.aspectRatio(1f)