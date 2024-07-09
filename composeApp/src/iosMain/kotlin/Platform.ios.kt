import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.Modifier
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun buttonSize(): Modifier = Modifier.aspectRatio(1f)