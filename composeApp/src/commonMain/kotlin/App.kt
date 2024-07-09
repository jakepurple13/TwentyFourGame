import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    settings: Settings,
) {
    MaterialTheme {
        Scaffold { padding ->
            TwentyFourGame(
                viewModel = viewModel { TwentyFourViewModel(settings = settings) },
                modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
            )
        }
    }
}

@Composable
fun TwentyFourGame(
    viewModel: TwentyFourViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    var showInstructions by rememberShowInstructions()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CalculatorDisplay(
                expression = viewModel.expression,
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

            AnimatedVisibility(
                viewModel.showAnswer
            ) {
                AnswerDisplay(viewModel.answer)
            }

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
                modifier = Modifier.padding(8.dp)
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
                    fontSize = 40.sp
                )
            }
        )
    }
}

@Composable
fun CalculatorDisplay(
    expression: String,
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
            modifier = Modifier.fillMaxWidth()
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
        itemsIndexed(fourNumbers) { index, action ->
            CalculatorButton(
                action = action,
                enabled = action.enabled,
                modifier = minSize,
                onClick = { onAction(action.action) }
            )
        }

        items(actions) { action ->
            CalculatorButton(
                action = action,
                modifier = minSize,
                onClick = { onAction(action.action) }
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
                modifier = minSize,
                onClick = onSubmit
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
                    modifier = minSize,
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
                    modifier = minSize,
                    onClick = onGiveUp
                )
            }
        }

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
                    modifier = minSize,
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
                    modifier = minSize,
                    onClick = noSolve
                )
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
                modifier = minSize,
                onClick = onShowInstructions
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