import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicMaterialThemeState

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun buttonSize(): Modifier = Modifier.sizeIn(50.dp, 50.dp)

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme {
    return rememberDynamicMaterialThemeState(Color(0xFF009DFF), isDarkMode).colorScheme
}

actual val canHaveAmoled: Boolean = false

actual val isMobile: Boolean = false