@file:OptIn(ExperimentalAdaptiveApi::class)

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.materialkolor.rememberDynamicColorScheme
import io.github.alexzhirkevich.cupertino.adaptive.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    settings: Settings,
) {
    AdaptiveTheme(
        material = MaterialThemeSpec.Default(
            colorScheme = buildColorScheme()
        ),
        cupertino = CupertinoThemeSpec.Default(),
    ) {
        TwentyFourGame(
            viewModel = viewModel { TwentyFourViewModel(settings = settings) },
        )
    }
}

@Composable
fun buildColorScheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
): ColorScheme {
    val themeColor by rememberThemeColor()
    val isAmoled by rememberIsAmoled()

    val animationSpec = spring<Color>(stiffness = Spring.StiffnessLow)

    return when (themeColor) {
        ThemeColor.Dynamic -> colorSchemeSetup(darkTheme, dynamicColor).let {
            if (isAmoled && darkTheme) {
                it.copy(
                    surface = Color.Black,
                    onSurface = Color.White,
                    background = Color.Black,
                    onBackground = Color.White
                )
            } else {
                it
            }
        }

        ThemeColor.Custom -> {
            val customColor by rememberCustomColor()
            rememberDynamicColorScheme(
                seedColor = customColor,
                isDark = darkTheme,
                isAmoled = isAmoled
            )
        }

        else -> rememberDynamicColorScheme(
            seedColor = themeColor.seedColor,
            isDark = darkTheme,
            isAmoled = isAmoled
        )
    }.let { colorScheme ->
        colorScheme.copy(
            primary = colorScheme.primary.animate(animationSpec),
            primaryContainer = colorScheme.primaryContainer.animate(animationSpec),
            secondary = colorScheme.secondary.animate(animationSpec),
            secondaryContainer = colorScheme.secondaryContainer.animate(animationSpec),
            tertiary = colorScheme.tertiary.animate(animationSpec),
            tertiaryContainer = colorScheme.tertiaryContainer.animate(animationSpec),
            background = colorScheme.background.animate(animationSpec),
            surface = colorScheme.surface.animate(animationSpec),
            surfaceTint = colorScheme.surfaceTint.animate(animationSpec),
            surfaceBright = colorScheme.surfaceBright.animate(animationSpec),
            surfaceDim = colorScheme.surfaceDim.animate(animationSpec),
            surfaceContainer = colorScheme.surfaceContainer.animate(animationSpec),
            surfaceContainerHigh = colorScheme.surfaceContainerHigh.animate(animationSpec),
            surfaceContainerHighest = colorScheme.surfaceContainerHighest.animate(animationSpec),
            surfaceContainerLow = colorScheme.surfaceContainerLow.animate(animationSpec),
            surfaceContainerLowest = colorScheme.surfaceContainerLowest.animate(animationSpec),
            surfaceVariant = colorScheme.surfaceVariant.animate(animationSpec),
            error = colorScheme.error.animate(animationSpec),
            errorContainer = colorScheme.errorContainer.animate(animationSpec),
            onPrimary = colorScheme.onPrimary.animate(animationSpec),
            onPrimaryContainer = colorScheme.onPrimaryContainer.animate(animationSpec),
            onSecondary = colorScheme.onSecondary.animate(animationSpec),
            onSecondaryContainer = colorScheme.onSecondaryContainer.animate(animationSpec),
            onTertiary = colorScheme.onTertiary.animate(animationSpec),
            onTertiaryContainer = colorScheme.onTertiaryContainer.animate(animationSpec),
            onBackground = colorScheme.onBackground.animate(animationSpec),
            onSurface = colorScheme.onSurface.animate(animationSpec),
            onSurfaceVariant = colorScheme.onSurfaceVariant.animate(animationSpec),
            onError = colorScheme.onError.animate(animationSpec),
            onErrorContainer = colorScheme.onErrorContainer.animate(animationSpec),
            inversePrimary = colorScheme.inversePrimary.animate(animationSpec),
            inverseSurface = colorScheme.inverseSurface.animate(animationSpec),
            inverseOnSurface = colorScheme.inverseOnSurface.animate(animationSpec),
            outline = colorScheme.outline.animate(animationSpec),
            outlineVariant = colorScheme.outlineVariant.animate(animationSpec),
            scrim = colorScheme.scrim.animate(animationSpec),
        )
    }
}

@Composable
private fun Color.animate(animationSpec: AnimationSpec<Color>): Color {
    return animateColorAsState(this, animationSpec).value
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwentyFourGame(
    viewModel: TwentyFourViewModel = viewModel(),
) {
    var showInstructions by rememberShowInstructions()

    var showSettings by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (showSettings) {
        SettingsSheet(
            sheetState = sheetState,
            onDismiss = {
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion { showSettings = false }
            }
        )
    }

    DrawerSheet(
        onDismiss = { scope.launch { drawerState.close() } },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(
                            onClick = { showInstructions = true }
                        ) { Icon(Icons.Default.Info, null) }

                        IconButton(
                            onClick = {
                                if (isMobile) showSettings = true
                                else scope.launch { drawerState.open() }
                            }
                        ) { Icon(Icons.Default.Settings, null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .padding(bottom = padding.calculateBottomPadding())
                    .fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    CalculatorDisplay(
                        expression = viewModel.expression,
                        fullExpression = viewModel.fullExpression,
                        error = viewModel.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 25.dp,
                                    bottomEnd = 25.dp
                                )
                            )
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        viewModel.showAnswer
                    ) { AnswerDisplay(viewModel.answer) }

                    Spacer(modifier = Modifier.height(8.dp))
                    CalculatorButtonGrid(
                        fourNumbers = viewModel.fourCalculate,
                        actions = calculatorActions(),
                        onAction = viewModel::onAction,
                        onSubmit = viewModel::submit,
                        onGiveUp = viewModel::giveUp,
                        onRestart = viewModel::restart,
                        showRestart = viewModel.showAnswer,
                        noSolve = viewModel::noSolve,
                        canSubmit = viewModel.canSubmit,
                        onUndo = viewModel::undo,
                        canUndo = viewModel.fullExpression.isNotEmpty(),
                        isHardMode = viewModel.isHardMode,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            if (showInstructions) {
                AdaptiveAlertDialog(
                    onDismissRequest = { showInstructions = false },
                    title = { Text("24 Game Instructions") },
                    message = { Text(RULES) },
                    buttons = {
                        action(
                            onClick = { showInstructions = false },
                            title = { Text("OK") },
                        )
                    },
                    adaptation = {
                        cupertino {
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AnswerDisplay(
    answer: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ListItem(
            overlineContent = {
                Text(
                    "Answer:",
                    fontSize = 20.sp
                )
            },
            headlineContent = {
                Text(
                    answer,
                    fontSize = 40.sp,
                    lineHeight = 30.sp
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorDisplay(
    expression: String,
    fullExpression: String,
    error: String? = null,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        val textFieldSize by autoSizeTextFieldStyle(
            expression,
            TextStyle(
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.End
            )
        )

        Column(
            modifier = Modifier.animateContentSize()
        ) {
            BasicTextField(
                value = expression,
                onValueChange = {},
                textStyle = TextStyle(
                    fontSize = animateFloatAsState(textFieldSize.value).value.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.End
                ),
                /*textStyle = TextStyle(
                    fontSize = 40.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.End
                ),*/
                maxLines = 1,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = {
                    TextFieldDefaults.DecorationBox(
                        value = expression,
                        innerTextField = it,
                        label = {
                            var multiplier by remember { mutableStateOf(1f) }

                            Text(
                                fullExpression,
                                fontSize = multiplier * 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                onTextLayout = { result ->
                                    if (result.hasVisualOverflow) {
                                        multiplier *= 0.99f // you can tune this constant
                                    }
                                }
                            )
                        },
                        enabled = true,
                        singleLine = true,
                        interactionSource = remember { MutableInteractionSource() },
                        visualTransformation = VisualTransformation.None,
                        container = {},
                        isError = error != null,
                    )
                },
            )

            AnimatedContent(targetState = error) { target ->
                target?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(
                            top = 8.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BoxWithConstraintsScope.autoSizeTextFieldStyle(
    text: String,
    textStyle: TextStyle = TextStyle.Default,
): MutableState<TextUnit> {
    val nFontSize = remember { mutableStateOf(textStyle.fontSize) }

    val calculateIntrinsics = @Composable {
        ParagraphIntrinsics(
            text = text,
            style = textStyle.copy(fontSize = nFontSize.value),
            density = LocalDensity.current,
            fontFamilyResolver = LocalFontFamilyResolver.current,
        )
    }

    var intrinsics = calculateIntrinsics()

    val maxInputWidth = maxWidth - 2 * 16.dp

    with(LocalDensity.current) {
        while (intrinsics.maxIntrinsicWidth > maxInputWidth.toPx()) {
            nFontSize.value *= 0.9f
            intrinsics = calculateIntrinsics()
        }
    }

    return nFontSize
}

@Composable
fun CalculatorButtonGrid(
    fourNumbers: List<CalculatorUiAction>,
    actions: List<CalculatorUiAction>,
    onAction: (CalculatorAction) -> Unit,
    onSubmit: () -> Unit,
    onGiveUp: () -> Unit,
    onRestart: () -> Unit,
    showRestart: Boolean,
    noSolve: () -> Unit,
    canSubmit: Boolean,
    onUndo: () -> Unit,
    canUndo: Boolean,
    isHardMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val useHaptics by rememberUseHaptic()
    val minSize = buttonSize()

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(fourNumbers) { action ->
            CalculatorButton(
                action = action,
                enabled = action.enabled,
                onClick = { onAction(action.action) },
                useHaptics = useHaptics,
                modifier = minSize
                    .animateItem()
                    .fillMaxWidth(),
            )
        }

        clearButton(
            onAction = onAction,
            useHaptics = useHaptics,
            minSize = minSize.fillMaxWidth()
        )

        items(actions) { action ->
            CalculatorButton(
                action = action,
                onClick = { onAction(action.action) },
                useHaptics = useHaptics,
                modifier = minSize
                    .animateItem()
                    .fillMaxWidth(),
            )
        }

        undoButton(
            canUndo = canUndo,
            onUndo = onUndo,
            useHaptics = useHaptics,
            minSize = minSize.fillMaxWidth(),
        )

        deleteButton(
            onAction = onAction,
            useHaptics = useHaptics,
            minSize = minSize.fillMaxWidth(),
        )

        giveUpButton(
            showRestart = showRestart,
            onRestart = onRestart,
            onGiveUp = onGiveUp,
            useHaptics = useHaptics,
            minSize = minSize.fillMaxWidth(),
        )

        noSolveButton(
            isHardMode = isHardMode,
            showRestart = showRestart,
            noSolve = noSolve,
            useHaptics = useHaptics,
            minSize = minSize.fillMaxWidth(),
        )

        item(span = { GridItemSpan(maxCurrentLineSpan - 1) }) {}

        submitButton(
            showRestart = showRestart,
            canSubmit = canSubmit,
            onSubmit = onSubmit,
            useHaptics = useHaptics,
            minSize = minSize.fillMaxWidth(),
        )
    }
}

fun LazyGridScope.submitButton(
    showRestart: Boolean,
    canSubmit: Boolean,
    minSize: Modifier,
    useHaptics: Boolean,
    onSubmit: () -> Unit,
) {
    item {
        CalculatorButton(
            action = CalculatorUiAction(
                text = "=",
                highlightLevel = HighlightLevel.StronglyHighlighted,
                action = CalculatorAction.Calculate
            ),
            enabled = !showRestart && canSubmit,
            modifier = minSize.animateItem(),
            onClick = onSubmit,
            useHaptics = useHaptics,
        )
    }
}

fun LazyGridScope.noSolveButton(
    isHardMode: Boolean,
    showRestart: Boolean,
    minSize: Modifier,
    useHaptics: Boolean,
    noSolve: () -> Unit,
) {
    if (isHardMode) {
        item {
            if (showRestart) {
                ToolTipWrapper(
                    text = { Text("Hide answer or help") }
                ) {
                    CalculatorButton(
                        action = CalculatorUiAction(
                            text = null,
                            content = { Icon(Icons.Default.HideSource, null) },
                            highlightLevel = HighlightLevel.StronglyHighlighted,
                            action = CalculatorAction.Calculate
                        ),
                        enabled = showRestart,
                        modifier = minSize.animateItem(),
                        onClick = noSolve,
                        useHaptics = useHaptics,
                    )
                }
            } else {
                ToolTipWrapper(
                    text = { Text("Check if there is a solution") }
                ) {
                    CalculatorButton(
                        action = CalculatorUiAction(
                            text = null,
                            content = { Icon(Icons.Default.QuestionMark, null) },
                            highlightLevel = HighlightLevel.StronglyHighlighted,
                            action = CalculatorAction.Calculate
                        ),
                        enabled = !showRestart,
                        modifier = minSize.animateItem(),
                        onClick = noSolve,
                        useHaptics = useHaptics,
                    )
                }
            }
        }
    }
}

fun LazyGridScope.giveUpButton(
    showRestart: Boolean,
    minSize: Modifier,
    onRestart: () -> Unit,
    onGiveUp: () -> Unit,
    useHaptics: Boolean,
) {
    item {
        if (showRestart) {
            ToolTipWrapper(
                text = { Text("Restart with new numbers") }
            ) {
                CalculatorButton(
                    action = CalculatorUiAction(
                        text = null,
                        content = { Icon(Icons.Default.RestartAlt, null) },
                        highlightLevel = HighlightLevel.StronglyHighlighted,
                        action = CalculatorAction.Calculate
                    ),
                    modifier = minSize.animateItem(),
                    onClick = onRestart,
                    useHaptics = useHaptics,
                )
            }
        } else {
            ToolTipWrapper(
                text = { Text("Give up and see a solution") }
            ) {
                CalculatorButton(
                    action = CalculatorUiAction(
                        text = null,
                        content = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                        highlightLevel = HighlightLevel.StronglyHighlighted,
                        action = CalculatorAction.Calculate
                    ),
                    modifier = minSize.animateItem(),
                    onClick = onGiveUp,
                    useHaptics = useHaptics,
                )
            }
        }
    }
}

fun LazyGridScope.deleteButton(
    minSize: Modifier,
    useHaptics: Boolean,
    onAction: (CalculatorAction) -> Unit,
) {
    item {
        ToolTipWrapper(
            text = { Text("Delete") }
        ) {
            CalculatorButton(
                CalculatorUiAction(
                    text = null,
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    highlightLevel = HighlightLevel.Neutral,
                    action = CalculatorAction.Delete
                ),
                modifier = minSize.animateItem(),
                onClick = { onAction(CalculatorAction.Delete) },
                useHaptics = useHaptics,
            )
        }
    }
}

fun LazyGridScope.clearButton(
    minSize: Modifier,
    useHaptics: Boolean,
    onAction: (CalculatorAction) -> Unit,
) {
    item {
        ToolTipWrapper(
            title = { Text("All Clear") },
            text = { Text("Clear current equation") }
        ) {
            CalculatorButton(
                CalculatorUiAction(
                    text = "AC",
                    highlightLevel = HighlightLevel.Highlighted,
                    action = CalculatorAction.Clear
                ),
                modifier = minSize.animateItem(),
                onClick = { onAction(CalculatorAction.Clear) },
                useHaptics = useHaptics,
            )
        }
    }

}

fun LazyGridScope.undoButton(
    canUndo: Boolean,
    minSize: Modifier,
    useHaptics: Boolean,
    onUndo: () -> Unit,
) {
    item {
        ToolTipWrapper(
            text = { Text("Bring back the last equation") }
        ) {
            CalculatorButton(
                action = CalculatorUiAction(
                    text = null,
                    content = { Icon(UndoIcon, null) },
                    highlightLevel = HighlightLevel.StronglyHighlighted,
                    action = CalculatorAction.Calculate
                ),
                enabled = canUndo,
                modifier = minSize.animateItem(),
                onClick = onUndo,
                useHaptics = useHaptics,
            )
        }
    }
}

@Composable
fun CalculatorButton(
    action: CalculatorUiAction,
    useHaptics: Boolean = true,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && useHaptics) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    if (action.text != null) {
        TextButton(
            onClick,
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.textButtonColors(
                containerColor = when (action.highlightLevel) {
                    HighlightLevel.Neutral -> MaterialTheme.colorScheme.surfaceVariant
                    HighlightLevel.SemiHighlighted -> MaterialTheme.colorScheme.inverseSurface
                    HighlightLevel.Highlighted -> MaterialTheme.colorScheme.tertiary
                    HighlightLevel.StronglyHighlighted -> MaterialTheme.colorScheme.primary
                },
                contentColor = when (action.highlightLevel) {
                    is HighlightLevel.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
                    is HighlightLevel.SemiHighlighted -> MaterialTheme.colorScheme.inverseOnSurface
                    is HighlightLevel.Highlighted -> MaterialTheme.colorScheme.onTertiary
                    is HighlightLevel.StronglyHighlighted -> MaterialTheme.colorScheme.onPrimary
                }
            ),
            interactionSource = interactionSource,
            modifier = modifier
        ) {
            Text(
                text = action.text,
                fontSize = 36.sp,
                textAlign = TextAlign.Center,
                color = when (action.highlightLevel) {
                    is HighlightLevel.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
                    is HighlightLevel.SemiHighlighted -> MaterialTheme.colorScheme.inverseOnSurface
                    is HighlightLevel.Highlighted -> MaterialTheme.colorScheme.onTertiary
                    is HighlightLevel.StronglyHighlighted -> MaterialTheme.colorScheme.onPrimary
                }
            )
        }
    } else {
        IconButton(
            onClick,
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = when (action.highlightLevel) {
                    HighlightLevel.Neutral -> MaterialTheme.colorScheme.surfaceVariant
                    HighlightLevel.SemiHighlighted -> MaterialTheme.colorScheme.inverseSurface
                    HighlightLevel.Highlighted -> MaterialTheme.colorScheme.tertiary
                    HighlightLevel.StronglyHighlighted -> MaterialTheme.colorScheme.primary
                },
                contentColor = when (action.highlightLevel) {
                    is HighlightLevel.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
                    is HighlightLevel.SemiHighlighted -> MaterialTheme.colorScheme.inverseOnSurface
                    is HighlightLevel.Highlighted -> MaterialTheme.colorScheme.onTertiary
                    is HighlightLevel.StronglyHighlighted -> MaterialTheme.colorScheme.onPrimary
                },
                disabledContainerColor = when (action.highlightLevel) {
                    HighlightLevel.Neutral -> MaterialTheme.colorScheme.surfaceVariant
                    HighlightLevel.SemiHighlighted -> MaterialTheme.colorScheme.inverseSurface
                    HighlightLevel.Highlighted -> MaterialTheme.colorScheme.tertiary
                    HighlightLevel.StronglyHighlighted -> MaterialTheme.colorScheme.primary
                }.copy(alpha = .5f)
            ),
            interactionSource = interactionSource,
            modifier = modifier
        ) {
            action.content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolTipWrapper(
    title: (@Composable () -> Unit)? = null,
    text: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = title,
                text = text
            )
        },
        state = rememberTooltipState()
    ) { content() }
}

fun calculatorActions() = listOf(
    CalculatorUiAction(
        text = "()",
        highlightLevel = HighlightLevel.SemiHighlighted,
        action = CalculatorAction.Parentheses
    ),
    CalculatorUiAction(
        text = "÷",
        highlightLevel = HighlightLevel.SemiHighlighted,
        action = CalculatorAction.Op(Operation.DIVIDE)
    ),
    CalculatorUiAction(
        text = "x",
        highlightLevel = HighlightLevel.SemiHighlighted,
        action = CalculatorAction.Op(Operation.MULTIPLY)
    ),
    CalculatorUiAction(
        text = "-",
        highlightLevel = HighlightLevel.SemiHighlighted,
        action = CalculatorAction.Op(Operation.SUBTRACT)
    ),
    CalculatorUiAction(
        text = "+",
        highlightLevel = HighlightLevel.SemiHighlighted,
        action = CalculatorAction.Op(Operation.ADD)
    ),
)