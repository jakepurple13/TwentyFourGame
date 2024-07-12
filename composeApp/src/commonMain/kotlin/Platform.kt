import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun buttonSize(): Modifier

@Composable
expect fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme

expect val canHaveAmoled: Boolean

expect val isMobile: Boolean