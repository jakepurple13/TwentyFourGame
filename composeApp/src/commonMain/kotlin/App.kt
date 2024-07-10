import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    settings: Settings,
) {
    TwentyFourTheme {
        TwentyFourGame(
            viewModel = viewModel { TwentyFourViewModel(settings = settings) },
        )
    }
}

@Composable
fun TwentyFourTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val themeColor by rememberThemeColor()

    val animationSpec = spring<Color>(stiffness = Spring.StiffnessLow)

    val colorScheme = when (themeColor) {
        ThemeColor.Dynamic -> colorSchemeSetup(darkTheme, dynamicColor)
        else -> rememberDynamicColorScheme(themeColor.seedColor, darkTheme)
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

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(
                        onClick = { showSettings = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                CalculatorDisplay(
                    expression = viewModel.expression,
                    fullExpression = viewModel.fullExpression,
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
                    onShowInstructions = { showInstructions = true },
                    canSubmit = viewModel.canSubmit,
                    onUndo = viewModel::undo,
                    canUndo = viewModel.fullExpression.isNotEmpty(),
                    isHardMode = viewModel.isHardMode,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        if (showInstructions) {
            AlertDialog(
                onDismissRequest = { showInstructions = false },
                title = { Text("24 Game Instructions") },
                text = {
                    Text(
                        """
                        The 24 puzzle is an arithmetical puzzle in which the objective is to find a way to manipulate four integers so that the end result is 24.
                        For example, for the numbers 4, 7, 8, 8, a possible solution is 
                        (7 - (8/8)) * 4 = 24
                        
                        There might be multiple solutions!
                        
                        If you enable Hard Mode, it is possible that there is no solution.
                    """.trimIndent()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showInstructions = false }
                    ) { Text("OK") }
                }
            )
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = expression,
            onValueChange = {},
            textStyle = TextStyle(
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.End
            ),
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
                        Text(
                            fullExpression,
                            fontSize = 20.sp,
                        )
                    },
                    enabled = true,
                    singleLine = true,
                    interactionSource = remember { MutableInteractionSource() },
                    visualTransformation = VisualTransformation.None,
                    container = {}
                )
            },
        )
    }
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
    onShowInstructions: () -> Unit,
    canSubmit: Boolean,
    onUndo: () -> Unit,
    canUndo: Boolean,
    isHardMode: Boolean,
    modifier: Modifier = Modifier,
) {
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
                modifier = minSize.animateItem(),
                onClick = { onAction(action.action) }
            )
        }

        items(actions) { action ->
            CalculatorButton(
                action = action,
                modifier = minSize.animateItem(),
                onClick = { onAction(action.action) }
            )
        }

        item {
            CalculatorButton(
                action = CalculatorUiAction(
                    text = null,
                    content = { Icon(UndoIcon, null) },
                    highlightLevel = HighlightLevel.StronglyHighlighted,
                    action = CalculatorAction.Calculate
                ),
                enabled = canUndo,
                modifier = minSize.animateItem(),
                onClick = onUndo
            )
        }

        item {
            if (showRestart) {
                CalculatorButton(
                    action = CalculatorUiAction(
                        text = null,
                        content = { Icon(Icons.Default.RestartAlt, null) },
                        highlightLevel = HighlightLevel.StronglyHighlighted,
                        action = CalculatorAction.Calculate
                    ),
                    modifier = minSize.animateItem(),
                    onClick = onRestart
                )
            } else {
                CalculatorButton(
                    action = CalculatorUiAction(
                        text = null,
                        content = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                        highlightLevel = HighlightLevel.StronglyHighlighted,
                        action = CalculatorAction.Calculate
                    ),
                    modifier = minSize.animateItem(),
                    onClick = onGiveUp
                )
            }
        }

        if (isHardMode) {
            item {
                if (showRestart) {
                    CalculatorButton(
                        action = CalculatorUiAction(
                            text = null,
                            content = { Icon(Icons.Default.HideSource, null) },
                            highlightLevel = HighlightLevel.StronglyHighlighted,
                            action = CalculatorAction.Calculate
                        ),
                        enabled = showRestart,
                        modifier = minSize.animateItem(),
                        onClick = noSolve
                    )
                } else {
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
                    )
                }
            }
        }

        item {
            CalculatorButton(
                action = CalculatorUiAction(
                    text = null,
                    content = { Icon(Icons.Default.Info, null) },
                    highlightLevel = HighlightLevel.StronglyHighlighted,
                    action = CalculatorAction.Calculate
                ),
                modifier = minSize.animateItem(),
                onClick = onShowInstructions
            )
        }

        item {
            CalculatorButton(
                action = CalculatorUiAction(
                    text = "✓",
                    highlightLevel = HighlightLevel.StronglyHighlighted,
                    action = CalculatorAction.Calculate
                ),
                enabled = !showRestart && canSubmit,
                modifier = minSize.animateItem(),
                onClick = onSubmit
            )
        }
    }
}

@Composable
fun CalculatorButton(
    action: CalculatorUiAction,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
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
                }
            ),
            modifier = modifier
        ) {
            action.content()
        }
    }
}