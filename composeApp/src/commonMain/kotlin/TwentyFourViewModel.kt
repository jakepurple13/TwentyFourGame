import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class TwentyFourViewModel(
    private val writer: ExpressionWriter = ExpressionWriter(),
    private val settings: Settings,
) : ViewModel() {

    var answer by mutableStateOf("")
        private set

    private var fourNumbers by mutableStateOf(randomNumbers())

    private val enabledNumbers = mutableStateListOf(true, true, true, true)

    var error by mutableStateOf<String?>(null)
        private set

    private val lastNumberIndex = mutableListOf<CalculatorAction>()

    val fourCalculate by derivedStateOf {
        fourNumbers.mapIndexed { index, i ->
            CalculatorUiAction(
                text = "$i",
                highlightLevel = HighlightLevel.Neutral,
                enabled = enabledNumbers.getOrElse(index) { true },
                action = CalculatorAction.Number(i, index)
            )
        }
    }

    var showAnswer by mutableStateOf(false)
        private set

    val canSubmit by derivedStateOf {
        enabledNumbers.all { !it }
    }

    var fullExpression by mutableStateOf("")
        private set

    var expression by mutableStateOf("")
        private set

    var isHardMode by mutableStateOf(false)
        private set

    init {
        settings.currentNumbers()
            .onEach { fourNumbers = it }
            .launchIn(viewModelScope)

        settings.hardMode()
            .onEach { isHardMode = it }
            .launchIn(viewModelScope)
    }

    fun onAction(action: CalculatorAction) {
        error = null

        when (action) {
            CalculatorAction.Clear -> {
                for (i in enabledNumbers.indices) {
                    enabledNumbers[i] = true
                }
                lastNumberIndex.clear()
            }

            CalculatorAction.Delete -> {
                lastNumberIndex.removeLastOrNull()?.let {
                    if (it is CalculatorAction.Number) {
                        enabledNumbers[it.index] = true
                    }
                }
            }

            is CalculatorAction.Number -> {
                if (lastNumberIndex.lastOrNull() is CalculatorAction.Number) return
                if (lastNumberIndex.lastOrNull() is CalculatorAction.Parentheses && expression.lastOrNull() == ')') return
                lastNumberIndex.add(action)
                enabledNumbers[action.index] = false
            }

            is CalculatorAction.Op -> {
                if (lastNumberIndex.lastOrNull() is CalculatorAction.Op) return
                lastNumberIndex.add(action)
            }

            is CalculatorAction.Parentheses -> {
                if (writer.canProcessParentheses()) {
                    lastNumberIndex.add(action)
                } else {
                    return
                }
            }

            else -> {
                lastNumberIndex.add(action)
            }
        }

        writer.processAction(action)
        expression = writer.expression
    }

    fun giveUp() {
        val stringBuilder = StringBuilder()
        answer = if (solve24(fourNumbers, stringBuilder)) {
            stringBuilder.toString()
        } else {
            "No Solution"
        }

        showAnswer = true
    }

    fun restart() {
        viewModelScope.launch {
            var newNumbers = randomNumbers()
            if (!isHardMode) {
                while (!solve24(newNumbers)) {
                    newNumbers = randomNumbers()
                }
            }
            settings.updateCurrentNumbers(newNumbers)
        }
        showAnswer = false
        answer = ""
        expression = ""
        fullExpression = ""
        for (i in enabledNumbers.indices) {
            enabledNumbers[i] = true
        }
        writer.processAction(CalculatorAction.Clear)
        lastNumberIndex.clear()
    }

    fun submit() {
        if (enabledNumbers.all { !it }) {
            fullExpression = expression
            runCatching {
                writer.processAction(CalculatorAction.Calculate)
                expression = writer.expression
            }
                .onSuccess {
                    expression
                        .toDoubleOrNull()
                        ?.roundToInt()
                        ?.let {
                            if (it == SOLVE_GOAL) {
                                showAnswer = true
                                answer = "Correct!"
                            }
                        }
                }
                .onFailure {
                    error = "Invalid Input"
                    expression = ""
                }
        }
    }

    fun noSolve() {
        if (showAnswer) {
            showAnswer = false
        } else {
            if (solve24(fourNumbers)) {
                showAnswer = true
                answer = "There is a solution."
            } else {
                showAnswer = true
                answer = "There is no solution."
            }
        }
    }

    fun undo() {
        writer.expression = fullExpression
        expression = fullExpression
    }
}

data class CalculatorUiAction(
    val text: String?,
    val highlightLevel: HighlightLevel,
    val action: CalculatorAction,
    val enabled: Boolean = true,
    val content: @Composable () -> Unit = {},
)

sealed interface HighlightLevel {
    data object Neutral : HighlightLevel
    data object SemiHighlighted : HighlightLevel
    data object Highlighted : HighlightLevel
    data object StronglyHighlighted : HighlightLevel
}
