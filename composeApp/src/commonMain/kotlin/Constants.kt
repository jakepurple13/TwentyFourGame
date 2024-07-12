import kotlin.random.Random

const val N_CARDS = 4
const val SOLVE_GOAL = 24
const val MAX_DIGIT = 9

fun randomNumbers() = IntArray(N_CARDS) { Random.nextInt(1, MAX_DIGIT + 1) }

val RULES = """
    The 24 puzzle is an arithmetical puzzle in which the objective is to find a way to manipulate four integers so that the end result is 24.
    For example, for the numbers 4, 7, 8, 8, a possible solution is 
    (7 - (8/8)) * 4 = 24
    
    There might be multiple solutions!
    
    If you enable Hard Mode, it is possible that there is no solution.
""".trimIndent()