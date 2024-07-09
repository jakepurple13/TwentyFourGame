package com.programmersbox.twentyfourgame

import App
import Settings
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TwentyFourGameTheme {
                val context = LocalContext.current
                App(
                    settings = remember { Settings { context.filesDir.resolve(Settings.DATA_STORE_FILE_NAME).absolutePath } }
                )
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    TwentyFourGameTheme {
        val context = LocalContext.current
        App(
            settings = remember { Settings { context.filesDir.resolve(Settings.DATA_STORE_FILE_NAME).absolutePath } }
        )
    }
}

@Composable
fun TwentyFourGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}