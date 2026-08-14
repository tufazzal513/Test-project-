package com.example.calculator.engine

import com.example.calculator.model.AngleMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object ExpressionEvaluator {

    private val formatSymbols = DecimalFormatSymbols(Locale.US)
    private val decimalFormat = DecimalFormat("#,##0.########", formatSymbols)
    private val scientificFormat = DecimalFormat("0.######E0", formatSymbols)

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    fun evaluate(rawExpr: String, angleMode: AngleMode): EvalResult {
        if (rawExpr.isBlank()) return EvalResult.Success(0.0, "0")

        val sanitized = sanitize(rawExpr)
        if (sanitized.isEmpty()) return EvalResult.Success(0.0, "0")

        return try {
            val parser = Parser(sanitized, angleMode)
            val result = parser.parse()
            if (result.isNaN()) {
                EvalResult.Error("Invalid calculation")
            } else if (result.isInfinite()) {
                EvalResult.Error("Cannot divide by 0")
            } else {
                EvalResult.Success(result, formatResult(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: Exception) {
            EvalResult.Error("Invalid expression")
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        val absVal = abs(value)
        return if (absVal != 0.0 && (absVal >= 1e12 || absVal < 1e-6)) {
            scientificFormat.format(value)
        } else {
            // Check if effectively integer
            if (value == floor(value) && !value.isInfinite() && absVal < 1e15) {
                String.format(Locale.US, "%,d", value.toLong())
            } else {
                decimalFormat.format(value)
            }
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "${Math.PI}")
            .replace("e", "${Math.E}")
            .replace("√", "sqrt")
            .replace(" ", "")
    }

    private class Parser(private val src: String, private val angleMode: AngleMode) {
        private var pos = 0
        private var ch = if (src.isNotEmpty()) src[0] else '\u0000'

        private fun nextChar() {
            pos++
            ch = if (pos < src.length) src[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            val x = parseExpression()
            if (pos < src.length) throw RuntimeException("Unexpected char: '$ch'")
            return x
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor | term `%` factor
        // factor = `+` factor | `-` factor | `(` expression `)` | number | functionName factor | factor `^` factor | factor `!`

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Cannot divide by 0")
                        x /= divisor
                    }
                    eat('%') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Cannot divide by 0")
                        x %= divisor
                    }
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+')) return +parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos

            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if (ch in '0'..'9' || ch == '.') {
                while (ch in '0'..'9' || ch == '.') nextChar()
                val numStr = src.substring(startPos, pos)
                x = numStr.toDoubleOrNull() ?: throw RuntimeException("Invalid number: $numStr")
            } else if (ch in 'a'..'z') {
                while (ch in 'a'..'z') nextChar()
                val func = src.substring(startPos, pos)
                val arg = parseFactor()
                x = when (func) {
                    "sqrt" -> {
                        if (arg < 0) throw ArithmeticException("Square root of negative number")
                        sqrt(arg)
                    }
                    "sin" -> {
                        val radians = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                        sin(radians)
                    }
                    "cos" -> {
                        val radians = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                        cos(radians)
                    }
                    "tan" -> {
                        val radians = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                        if (abs(cos(radians)) < 1e-12) throw ArithmeticException("Undefined tan")
                        tan(radians)
                    }
                    "log" -> {
                        if (arg <= 0) throw ArithmeticException("Log of non-positive number")
                        log10(arg)
                    }
                    "ln" -> {
                        if (arg <= 0) throw ArithmeticException("Ln of non-positive number")
                        ln(arg)
                    }
                    "abs" -> abs(arg)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected char: '$ch'")
            }

            if (eat('^')) {
                val exponent = parseFactor()
                x = x.pow(exponent)
            }

            if (eat('!')) {
                x = factorial(x)
            }

            return x
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != floor(n) || n > 170) {
                throw ArithmeticException("Factorial invalid input")
            }
            var res = 1.0
            val intN = n.toInt()
            for (i in 2..intN) {
                res *= i
            }
            return res
        }
    }
}
