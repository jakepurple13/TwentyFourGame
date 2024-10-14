import kotlin.random.Random

const val N_CARDS = 4
const val SOLVE_GOAL = 24
const val MAX_DIGIT = 9

fun randomNumbers() = IntArray(N_CARDS) { Random.nextInt(1, MAX_DIGIT + 1) }

val RULES = """
    The 24 puzzle is an arithmetical puzzle in which the objective is to find a way to manipulate four integers so that the end result is 24.
    
    Add, subtract, multiply, or divide four random numbers to get the answer to equal 24.
    
    Here's an example:
    4, 7, 8, 8
    Possible Solution:
    (7 - ( 8 / 8 )) * 4 = 24
   
    There may be more than one right answer.
    
    Play hard mode and get puzzles with no solution!
""".trimIndent()