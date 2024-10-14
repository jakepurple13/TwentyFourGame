import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
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
import com.materialkolor.ktx.from
import com.materialkolor.palettes.TonalPalette
import com.materialkolor.rememberDynamicColorScheme
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveAlertDialog
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerSheet(
    onDismiss: () -> Unit,
    drawerState: DrawerState,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                TopAppBar(
                    title = { Text("Settings") },
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                SheetContent()
            }
        },
        gesturesEnabled = false,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
        SheetContent()
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalAdaptiveApi::class)
@Composable
private fun SheetContent() {
    var isAmoled by rememberIsAmoled()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .navigationBarsPadding()
            .padding(vertical = 8.dp)
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

        var useHaptic by rememberUseHaptic()

        Card(
            onClick = { useHaptic = !useHaptic },
        ) {
            ListItem(
                headlineContent = { Text("Use Haptics") },
                trailingContent = { Switch(checked = useHaptic, onCheckedChange = { useHaptic = it }) },
            )
        }

        HorizontalDivider()

        var showThemes by remember { mutableStateOf(false) }
        Card(
            onClick = { showThemes = !showThemes },
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
            )

            var customColor by rememberCustomColor()

            var showColorPicker by remember { mutableStateOf(false) }

            if (showColorPicker) {
                AdaptiveAlertDialog(
                    onDismissRequest = { showColorPicker = false },
                    title = { Text("Custom Color") },
                    message = {
                        Column {
                            Text("Select a color to use as the background color of the app.")
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
                    },
                    buttons = {
                        action(
                            onClick = { showColorPicker = false },
                            title = { Text("Done") },
                        )
                    },
                )
            }

            AnimatedVisibility(showThemes) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    ThemeColor
                        .entries
                        .filter { it != ThemeColor.Custom }
                        .forEach {
                            ThemeItem(
                                onClick = { themeColor = it },
                                selected = themeColor == it,
                                themeColor = it,
                                colorScheme = if (it == ThemeColor.Dynamic)
                                    MaterialTheme.colorScheme
                                else
                                    rememberDynamicColorScheme(
                                        it.seedColor,
                                        isAmoled = isAmoled,
                                        isDark = isSystemInDarkTheme()
                                    )
                            )
                        }

                    ThemeItem(
                        onClick = {
                            themeColor = ThemeColor.Custom
                            showColorPicker = true
                        },
                        selected = themeColor == ThemeColor.Custom,
                        themeColor = ThemeColor.Custom,
                        colorScheme = rememberDynamicColorScheme(
                            customColor,
                            isAmoled = isAmoled,
                            isDark = isSystemInDarkTheme()
                        )
                    )
                }
            }
        }

        if (canHaveAmoled) {
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

@Composable
fun ThemeItem(
    onClick: () -> Unit,
    selected: Boolean,
    themeColor: ThemeColor,
    colorScheme: ColorScheme,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            SelectableMiniPalette(
                selected = selected,
                colorScheme = colorScheme
            )

            Text(themeColor.name)
        }
    }
}

@Composable
fun SelectableMiniPalette(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    colorScheme: ColorScheme,
) {
    SelectableMiniPalette(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        accents = remember(colorScheme) {
            listOf(
                TonalPalette.from(colorScheme.primary),
                TonalPalette.from(colorScheme.secondary),
                TonalPalette.from(colorScheme.tertiary)
            )
        }
    )
}

@Composable
fun SelectableMiniPaletteWithSurface(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    accents: List<TonalPalette>,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        SelectableMiniPalette(
            modifier = modifier,
            selected = selected,
            onClick = onClick,
            accents = accents
        )
    }
}

@Composable
fun SelectableMiniPalette(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    accents: List<TonalPalette>,
) {
    val content: @Composable () -> Unit = {
        Box {
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .offset((-25).dp, 25.dp),
                color = Color(accents[1].tone(85)),
            ) {}
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .offset(25.dp, 25.dp),
                color = Color(accents[2].tone(75)),
            ) {}
            val animationSpec = spring<Float>(stiffness = Spring.StiffnessMedium)
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(animationSpec) + fadeIn(animationSpec),
                exit = scaleOut(animationSpec) + fadeOut(animationSpec),
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Checked",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
    onClick?.let {
        Surface(
            onClick = onClick,
            modifier = modifier
                .padding(12.dp)
                .size(50.dp),
            shape = CircleShape,
            color = Color(accents[0].tone(60)),
        ) { content() }
    } ?: Surface(
        modifier = modifier
            .padding(12.dp)
            .size(50.dp),
        shape = CircleShape,
        color = Color(accents[0].tone(60)),
    ) { content() }
}