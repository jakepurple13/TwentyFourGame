import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class TwentyFourViewModel(
    private val writer: ExpressionWriter = ExpressionWriter(),
    private val settings: Settings,
) : ViewModel() {

    var answer by mutableStateOf("")

    private var fourNumbers by mutableStateOf(IntArray(4) { Random.nextInt(1, 10) })

    private val enabledNumbers = mutableStateListOf<Boolean>(true, true, true, true)

    private var lastNumberIndex = mutableListOf<Int>()

    val fourCalculate by derivedStateOf {
        fourNumbers.mapIndexed { index, i ->
            CalculatorUiAction(
                text = "$i",
                highlightLevel = HighlightLevel.Neutral,
                enabled = enabledNumbers.getOrElse(index) { true },
                action = CalculatorAction.Number(i)
            )
        }
    }

    var showAnswer by mutableStateOf(false)

    var expression by mutableStateOf("")
        private set

    init {
        settings.currentNumbers()
            .onEach { fourNumbers = it }
            .launchIn(viewModelScope)
    }

    fun onAction(action: CalculatorAction) {
        when (action) {
            CalculatorAction.Clear -> {
                for (i in enabledNumbers.indices) {
                    enabledNumbers[i] = true
                }
            }

            CalculatorAction.Delete -> {
                lastNumberIndex.removeLastOrNull()?.let {
                    enabledNumbers[it] = true
                }
            }

            else -> {}
        }
        writer.processAction(action)
        this.expression = writer.expression
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
            settings.updateCurrentNumbers(IntArray(4) { Random.nextInt(1, 10) })
        }
        showAnswer = false
        answer = ""
        expression = ""
        for (i in enabledNumbers.indices) {
            enabledNumbers[i] = true
        }
        writer.processAction(CalculatorAction.Clear)
        lastNumberIndex.clear()
    }

    fun submit() {
        writer.processAction(CalculatorAction.Calculate)
        expression = writer.expression
        expression
            .toDoubleOrNull()
            ?.roundToInt()
            ?.let {
                if (it == 24) {
                    showAnswer = true
                    answer = "Correct!"
                }
            }
    }

    fun onNumberPress(index: Int) {
        enabledNumbers[index] = false
        lastNumberIndex.add(index)
    }

    fun noSolve() {
        if(showAnswer) {
            showAnswer = false
        } else {
            if (solve24(fourNumbers, StringBuilder())) {
                showAnswer = true
                answer = "There is a solution."
            } else {
                showAnswer = true
                answer = "Correct!"
            }
        }
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

fun calculatorActions() = listOf(
    CalculatorUiAction(
        text = "AC",
        highlightLevel = HighlightLevel.Highlighted,
        action = CalculatorAction.Clear
    ),
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
    /*CalculatorUiAction(
        text = "=",
        highlightLevel = HighlightLevel.StronglyHighlighted,
        action = CalculatorAction.Calculate
    ),*/
)