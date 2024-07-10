class Frac(val num: Int, val den: Int)

enum class OpType { NUM, ADD, SUB, MUL, DIV }

class Expr(
    var op: OpType = OpType.NUM,
    var left: Expr? = null,
    var right: Expr? = null,
    var value: Int = 0,
)

fun showExpr(e: Expr?, prec: OpType, isRight: Boolean, stringBuilder: StringBuilder?) {
    if (e == null) return
    val op = when (e.op) {
        OpType.NUM -> {
            print(e.value)
            stringBuilder?.append(e.value)
            return
        }

        OpType.ADD -> " + "
        OpType.SUB -> " - "
        OpType.MUL -> " x "
        OpType.DIV -> " / "
    }

    if ((e.op == prec && isRight) || e.op < prec) {
        print("(")
        stringBuilder?.append("(")
    }
    showExpr(e.left, e.op, false, stringBuilder)
    print(op)
    stringBuilder?.append(op)
    showExpr(e.right, e.op, true, stringBuilder)
    if ((e.op == prec && isRight) || e.op < prec) {
        print(")")
        stringBuilder?.append(")")
    }
}

fun evalExpr(e: Expr?): Frac {
    if (e == null) return Frac(0, 1)
    if (e.op == OpType.NUM) return Frac(e.value, 1)
    val l = evalExpr(e.left)
    val r = evalExpr(e.right)
    return when (e.op) {
        OpType.ADD -> Frac(l.num * r.den + l.den * r.num, l.den * r.den)
        OpType.SUB -> Frac(l.num * r.den - l.den * r.num, l.den * r.den)
        OpType.MUL -> Frac(l.num * r.num, l.den * r.den)
        OpType.DIV -> Frac(l.num * r.den, l.den * r.num)
        else -> throw IllegalArgumentException("Unknown op: ${e.op}")
    }
}

fun solve(
    ea: Array<Expr?>,
    len: Int,
    stringBuilder: StringBuilder?,
): Boolean {
    if (len == 1) {
        val final = evalExpr(ea[0])
        if (final.num == final.den * SOLVE_GOAL && final.den != 0) {
            showExpr(ea[0], OpType.NUM, false, stringBuilder)
            return true
        }
    }

    val ex = arrayOfNulls<Expr>(N_CARDS)
    for (i in 0 until len - 1) {
        for (j in i + 1 until len) ex[j - 1] = ea[j]
        val node = Expr()
        ex[i] = node
        for (j in i + 1 until len) {
            node.left = ea[i]
            node.right = ea[j]
            for (k in OpType.entries.drop(1)) {
                node.op = k
                if (solve(ex, len - 1, stringBuilder)) return true
            }
            node.left = ea[j]
            node.right = ea[i]
            node.op = OpType.SUB
            if (solve(ex, len - 1, stringBuilder)) return true
            node.op = OpType.DIV
            if (solve(ex, len - 1, stringBuilder)) return true
            ex[j] = ea[j]
        }
        ex[i] = ea[i]
    }
    return false
}

fun solve24(
    n: IntArray,
    stringBuilder: StringBuilder? = null,
) = solve(Array(N_CARDS) { Expr(value = n[it]) }, N_CARDS, stringBuilder)
