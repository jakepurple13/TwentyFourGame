import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

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
            windowInsets = WindowInsets(0.dp),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
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

            if (canHaveAmoled) {
                var isAmoled by rememberIsAmoled()
                Card(
                    onClick = { isAmoled = !isAmoled },
                ) {
                    ListItem(
                        headlineContent = { Text("Enable Amoled") },
                        trailingContent = { Switch(checked = isAmoled, onCheckedChange = { isAmoled = it }) },
                        supportingContent = {
                            Text("This will make backgrounds and surfaces black to save battery on AMOLED screens.")
                        },
                        modifier = Modifier.clickable { isAmoled = !isAmoled }
                    )
                }
            }

            var showThemes by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.animateContentSize()
            ) {
                var themeColor by rememberThemeColor()

                ListItem(
                    headlineContent = { Text("Theme") },
                    trailingContent = { Text(themeColor.name) },
                    supportingContent = { Text("Select a theme to use in the app.") },
                    modifier = Modifier.clickable { showThemes = !showThemes }
                )

                var customColor by rememberCustomColor()

                AnimatedVisibility(showThemes) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ThemeColor
                            .entries
                            .filter { it != ThemeColor.Custom }
                            .forEach {
                                SuggestionChip(
                                    onClick = { themeColor = it },
                                    label = { Text(it.name) },
                                    icon = {
                                        ColorBox(
                                            color = if (it == ThemeColor.Dynamic)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                it.seedColor
                                        )
                                    },
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        true,
                                        borderColor = if (it == ThemeColor.Dynamic)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            it.seedColor
                                    )
                                )
                            }

                        SuggestionChip(
                            onClick = { themeColor = ThemeColor.Custom },
                            label = { Text(ThemeColor.Custom.name) },
                            icon = { ColorBox(color = customColor) },
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                true,
                                borderColor = customColor
                            )
                        )
                    }
                }

                AnimatedVisibility(
                    showThemes && themeColor == ThemeColor.Custom
                ) {
                    val controller = rememberColorPickerController()
                    HsvColorPicker(
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            customColor = colorEnvelope.color
                        },
                        controller = controller,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                            .padding(10.dp),
                    )
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
            .size(20.dp)
    )
}