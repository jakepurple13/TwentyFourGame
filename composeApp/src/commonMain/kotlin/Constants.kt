import kotlin.random.Random

const val N_CARDS = 4
const val SOLVE_GOAL = 24
const val MAX_DIGIT = 9

fun randomNumbers() = IntArray(N_CARDS) { Random.nextInt(1, MAX_DIGIT + 1) }