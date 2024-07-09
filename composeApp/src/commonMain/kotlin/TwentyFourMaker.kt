enum class Operation(val symbol: Char) {
    ADD('+'),
    SUBTRACT('-'),
    MULTIPLY('x'),
    DIVIDE('/'),
    PERCENT('%'),
}

val operationSymbols = Operation.entries.map { it.symbol }.joinToString("")

fun operationFromSymbol(symbol: Char): Operation {
    return Operation.entries.find { it.symbol == symbol }
        ?: throw IllegalArgumentException("Invalid symbol")
}

class ExpressionWriter {

    var expression = ""

    fun processAction(action: CalculatorAction) {
        when (action) {
            CalculatorAction.Calculate -> {
                val parser = ExpressionParser(prepareForCalculation())
                val evaluator = ExpressionEvaluator(parser.parse())
                expression = evaluator.evaluate().toString()
            }

            CalculatorAction.Clear -> {
                expression = ""
            }

            CalculatorAction.Decimal -> {
                if (canEnterDecimal()) {
                    expression += "."
                }
            }

            CalculatorAction.Delete -> {
                expression = expression.dropLast(1)
            }

            is CalculatorAction.Number -> {
                expression += action.number
            }

            is CalculatorAction.Op -> {
                if (canEnterOperation(action.operation)) {
                    expression += action.operation.symbol
                }
            }

            CalculatorAction.Parentheses -> {
                processParentheses()
            }
        }
    }

    private fun prepareForCalculation(): String {
        val newExpression = expression.dropLastWhile {
            it in "$operationSymbols(."
        }
        if (newExpression.isEmpty()) {
            return "0"
        }
        return newExpression
    }

    private fun processParentheses() {
        val openingCount = expression.count { it == '(' }
        val closingCount = expression.count { it == ')' }
        expression += when {
            expression.isEmpty() ||
                    expression.last() in "$operationSymbols(" -> "("

            expression.last() in "0123456789)" &&
                    openingCount == closingCount -> return

            else -> ")"
        }
    }

    private fun canEnterDecimal(): Boolean {
        if (expression.isEmpty() || expression.last() in "$operationSymbols.()") {
            return false
        }
        return !expression.takeLastWhile {
            it in "0123456789."
        }.contains(".")
    }

    private fun canEnterOperation(operation: Operation): Boolean {
        if (operation in listOf(Operation.ADD, Operation.SUBTRACT)) {
            return expression.isEmpty() || expression.last() in "$operationSymbols()0123456789"
        }
        return expression.isNotEmpty() || expression.last() in "0123456789)"
    }
}

sealed interface ExpressionPart {
    data class Number(val number: Double) : ExpressionPart
    data class Op(val operation: Operation) : ExpressionPart
    data class Parentheses(val type: ParenthesesType) : ExpressionPart
}

sealed interface ParenthesesType {
    data object Opening : ParenthesesType
    data object Closing : ParenthesesType
}

class ExpressionParser(
    private val calculation: String,
) {

    fun parse(): List<ExpressionPart> {
        val result = mutableListOf<ExpressionPart>()

        var i = 0
        while (i < calculation.length) {
            val curChar = calculation[i]
            when {
                curChar in operationSymbols -> {
                    result.add(
                        ExpressionPart.Op(operationFromSymbol(curChar))
                    )
                }

                curChar.isDigit() -> {
                    i = parseNumber(i, result)
                    continue
                }

                curChar in "()" -> {
                    parseParentheses(curChar, result)
                }
            }
            i++
        }
        return result
    }

    private fun parseNumber(startingIndex: Int, result: MutableList<ExpressionPart>): Int {
        var i = startingIndex
        val numberAsString = buildString {
            while (i < calculation.length && calculation[i] in "0123456789.") {
                append(calculation[i])
                i++
            }
        }
        result.add(ExpressionPart.Number(numberAsString.toDouble()))
        return i
    }

    private fun parseParentheses(curChar: Char, result: MutableList<ExpressionPart>) {
        result.add(
            ExpressionPart.Parentheses(
                type = when (curChar) {
                    '(' -> ParenthesesType.Opening
                    ')' -> ParenthesesType.Closing
                    else -> throw IllegalArgumentException("Invalid parentheses type")
                }
            )
        )
    }
}

/**
 * Uses the following grammar
 * expression:	term | term + term | term − term
 * term:	factor | factor * factor | factor / factor | factor % factor
 * factor: number | (expression) | + factor | − factor
 */
class ExpressionEvaluator(
    private val expression: List<ExpressionPart>,
) {

    fun evaluate(): Double {
        return evalExpression(expression).value
    }

    private fun evalExpression(expression: List<ExpressionPart>): ExpressionResult {
        val result = evalTerm(expression)
        var remaining = result.remainingExpression
        var sum = result.value
        while (true) {
            when (remaining.firstOrNull()) {
                ExpressionPart.Op(Operation.ADD) -> {
                    val term = evalTerm(remaining.drop(1))
                    sum += term.value
                    remaining = term.remainingExpression
                }

                ExpressionPart.Op(Operation.SUBTRACT) -> {
                    val term = evalTerm(remaining.drop(1))
                    sum -= term.value
                    remaining = term.remainingExpression
                }

                else -> return ExpressionResult(remaining, sum)
            }
        }
    }

    private fun evalTerm(expression: List<ExpressionPart>): ExpressionResult {
        val result = evalFactor(expression)
        var remaining = result.remainingExpression
        var sum = result.value
        while (true) {
            when (remaining.firstOrNull()) {
                ExpressionPart.Op(Operation.MULTIPLY) -> {
                    val factor = evalFactor(remaining.drop(1))
                    sum *= factor.value
                    remaining = factor.remainingExpression
                }

                ExpressionPart.Op(Operation.DIVIDE) -> {
                    val factor = evalFactor(remaining.drop(1))
                    sum /= factor.value
                    remaining = factor.remainingExpression
                }

                ExpressionPart.Op(Operation.PERCENT) -> {
                    val factor = evalFactor(remaining.drop(1))
                    sum *= (factor.value / 100.0)
                    remaining = factor.remainingExpression
                }

                else -> return ExpressionResult(remaining, sum)
            }
        }
    }

    // A factor is either a number or an expression in parentheses
    // e.g. 5.0, -7.5, -(3+4*5)
    // But NOT something like 3 * 5, 4 + 5
    private fun evalFactor(expression: List<ExpressionPart>): ExpressionResult {
        return when (val part = expression.firstOrNull()) {
            ExpressionPart.Op(Operation.ADD) -> {
                evalFactor(expression.drop(1))
            }

            ExpressionPart.Op(Operation.SUBTRACT) -> {
                evalFactor(expression.drop(1)).run {
                    ExpressionResult(remainingExpression, -value)
                }
            }

            ExpressionPart.Parentheses(ParenthesesType.Opening) -> {
                evalExpression(expression.drop(1)).run {
                    ExpressionResult(remainingExpression.drop(1), value)
                }
            }

            ExpressionPart.Op(Operation.PERCENT) -> evalTerm(expression.drop(1))
            is ExpressionPart.Number -> ExpressionResult(
                remainingExpression = expression.drop(1),
                value = part.number
            )

            else -> throw RuntimeException("Invalid part")
        }
    }

    data class ExpressionResult(
        val remainingExpression: List<ExpressionPart>,
        val value: Double,
    )
}

sealed interface CalculatorAction {
    data class Number(val number: Int, val index: Int) : CalculatorAction
    data class Op(val operation: Operation) : CalculatorAction
    data object Clear : CalculatorAction
    data object Delete : CalculatorAction
    data object Parentheses : CalculatorAction
    data object Calculate : CalculatorAction
    data object Decimal : CalculatorAction
}