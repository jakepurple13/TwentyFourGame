import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Settings") },
            windowInsets = WindowInsets(0.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {

            var hardMode by rememberHardMode()
            Card(
                onClick = { hardMode = !hardMode },
            ) {
                ListItem(
                    headlineContent = { Text("Hard Mode") },
                    trailingContent = { Switch(checked = hardMode, onCheckedChange = { hardMode = it }) },
                    supportingContent = {
                        Text("If Hard Mode is enabled, it is possible that there is no solution.")
                    },
                    modifier = Modifier.clickable { hardMode = !hardMode }
                )
            }

            var showThemes by remember { mutableStateOf(false) }
            Card {
                var themeColor by rememberThemeColor()

                ListItem(
                    headlineContent = { Text("Theme") },
                    trailingContent = { Text(themeColor.name) },
                    supportingContent = { Text("Select a theme to use in the app.") },
                    modifier = Modifier.clickable { showThemes = !showThemes }
                )
                AnimatedVisibility(
                    showThemes
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ThemeColor.entries.forEach {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { themeColor = it }
                                    .width(80.dp)
                                    .border(
                                        width = 4.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                if (it == ThemeColor.Dynamic) {
                                    ColorBox(color = MaterialTheme.colorScheme.primary)
                                } else {
                                    ColorBox(color = it.seedColor)
                                }
                                Text(it.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorBox(color: Color) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(color)
            .size(40.dp)
    )
}