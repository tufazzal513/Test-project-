package com.example.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.engine.ExpressionEvaluator
import com.example.calculator.model.AngleMode
import com.example.calculator.model.CalculatorState
import com.example.calculator.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onDigitClick(digit: String) {
        _state.update { current ->
            val expr = if (current.finalResult.isNotEmpty() && current.expression == current.finalResult) {
                // Starting fresh after hitting equals
                digit
            } else {
                current.expression + digit
            }
            current.copy(
                expression = expr,
                finalResult = "",
                error = null
            )
        }
        updatePreview()
    }

    fun onOperatorClick(op: String) {
        _state.update { current ->
            var expr = current.expression
            if (expr.isEmpty()) {
                if (op == "−" || op == "-") {
                    expr = "−"
                } else if (current.finalResult.isNotEmpty()) {
                    expr = current.finalResult + " " + op + " "
                }
            } else {
                // If ending with another operator, replace it
                val trimmed = expr.trimEnd()
                if (trimmed.endsWith("+") || trimmed.endsWith("−") || trimmed.endsWith("×") || trimmed.endsWith("÷") || trimmed.endsWith("%") || trimmed.endsWith("^")) {
                    val lastSpace = trimmed.lastIndexOf(' ')
                    expr = if (lastSpace != -1) {
                        trimmed.substring(0, lastSpace) + " " + op + " "
                    } else {
                        op + " "
                    }
                } else {
                    expr = "$expr $op "
                }
            }
            current.copy(
                expression = expr,
                finalResult = "",
                error = null
            )
        }
        updatePreview()
    }

    fun onDecimalClick() {
        _state.update { current ->
            val expr = current.expression
            // Find current number segment
            val tokens = expr.split(' ', '+', '−', '-', '×', '*', '÷', '/', '%', '(', ')')
            val lastToken = tokens.lastOrNull() ?: ""
            if (!lastToken.contains('.')) {
                val newExpr = if (lastToken.isEmpty() || expr.endsWith(" ")) expr + "0." else expr + "."
                current.copy(expression = newExpr, finalResult = "", error = null)
            } else {
                current
            }
        }
        updatePreview()
    }

    fun onFunctionClick(fn: String) {
        _state.update { current ->
            val expr = if (current.finalResult.isNotEmpty() && current.expression == current.finalResult) {
                "$fn("
            } else {
                "${current.expression}$fn("
            }
            current.copy(expression = expr, finalResult = "", error = null)
        }
        updatePreview()
    }

    fun onConstantClick(c: String) {
        _state.update { current ->
            val expr = current.expression + c
            current.copy(expression = expr, finalResult = "", error = null)
        }
        updatePreview()
    }

    fun onParenthesisClick() {
        _state.update { current ->
            val expr = current.expression
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }

            val newExpr = if (expr.isEmpty() || expr.endsWith(" ") || expr.endsWith("(") || openCount <= closeCount) {
                "$expr("
            } else {
                "$expr)"
            }
            current.copy(expression = newExpr, finalResult = "", error = null)
        }
        updatePreview()
    }

    fun onFactorialClick() {
        _state.update { current ->
            if (current.expression.isNotEmpty() && !current.expression.endsWith(" ")) {
                current.copy(expression = current.expression + "!", finalResult = "", error = null)
            } else current
        }
        updatePreview()
    }

    fun onPowerClick() {
        _state.update { current ->
            if (current.expression.isNotEmpty() && !current.expression.endsWith(" ")) {
                current.copy(expression = current.expression + " ^ ", finalResult = "", error = null)
            } else current
        }
        updatePreview()
    }

    fun onSqrtClick() {
        _state.update { current ->
            current.copy(expression = current.expression + "√(", finalResult = "", error = null)
        }
        updatePreview()
    }

    fun onPercentClick() {
        _state.update { current ->
            if (current.expression.isNotEmpty()) {
                current.copy(expression = current.expression + " % ", finalResult = "", error = null)
            } else current
        }
        updatePreview()
    }

    fun onToggleSign() {
        _state.update { current ->
            val expr = current.expression.trim()
            if (expr.isEmpty()) return@update current

            // If it's a simple number
            val num = expr.toDoubleOrNull()
            if (num != null) {
                val negated = -num
                val formatted = ExpressionEvaluator.formatResult(negated).replace(",", "")
                current.copy(expression = formatted, finalResult = "")
            } else {
                // Wrap in negation
                if (expr.startsWith("-(") && expr.endsWith(")")) {
                    current.copy(expression = expr.substring(2, expr.length - 1))
                } else {
                    current.copy(expression = "-($expr)")
                }
            }
        }
        updatePreview()
    }

    fun onBackspaceClick() {
        _state.update { current ->
            val expr = current.expression
            if (expr.isEmpty()) return@update current

            val newExpr = if (expr.endsWith(" ")) {
                // If ending with operator like " + "
                val trimmed = expr.trimEnd()
                val lastSpace = trimmed.lastIndexOf(' ')
                if (lastSpace != -1) trimmed.substring(0, lastSpace) else ""
            } else if (expr.endsWith("sin(") || expr.endsWith("cos(") || expr.endsWith("tan(") || expr.endsWith("log(")) {
                expr.substring(0, expr.length - 4)
            } else if (expr.endsWith("sqrt(") || expr.endsWith("asin(") || expr.endsWith("acos(") || expr.endsWith("atan(")) {
                expr.substring(0, expr.length - 5)
            } else if (expr.endsWith("ln(") || expr.endsWith("√(")) {
                expr.substring(0, expr.length - 2)
            } else {
                expr.dropLast(1)
            }
            current.copy(expression = newExpr, finalResult = "", error = null)
        }
        updatePreview()
    }

    fun onClearAll() {
        _state.update { current ->
            current.copy(
                expression = "",
                previewResult = "",
                finalResult = "",
                error = null
            )
        }
    }

    fun onEqualsClick() {
        val current = _state.value
        val expr = current.expression
        if (expr.isBlank()) return

        when (val res = ExpressionEvaluator.evaluate(expr, current.angleMode)) {
            is ExpressionEvaluator.EvalResult.Success -> {
                val historyItem = HistoryItem(
                    expression = expr,
                    result = res.formatted
                )
                _state.update {
                    it.copy(
                        finalResult = res.formatted,
                        previewResult = "",
                        expression = res.formatted.replace(",", ""),
                        error = null,
                        history = listOf(historyItem) + it.history
                    )
                }
            }
            is ExpressionEvaluator.EvalResult.Error -> {
                _state.update {
                    it.copy(error = res.message, previewResult = "")
                }
            }
        }
    }

    fun toggleScientific() {
        _state.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun toggleAngleMode() {
        _state.update {
            val newMode = if (it.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
            it.copy(angleMode = newMode)
        }
        updatePreview()
    }

    fun toggleHistory() {
        _state.update { it.copy(showHistory = !it.showHistory) }
    }

    fun onHistoryItemClick(item: HistoryItem) {
        _state.update {
            it.copy(
                expression = item.result.replace(",", ""),
                finalResult = "",
                previewResult = "",
                showHistory = false,
                error = null
            )
        }
    }

    fun onClearHistory() {
        _state.update { it.copy(history = emptyList()) }
    }

    // Memory operations
    fun onMemoryClear() {
        _state.update { it.copy(memoryValue = 0.0, hasMemory = false) }
    }

    fun onMemoryRecall() {
        val mem = _state.value.memoryValue
        val formatted = ExpressionEvaluator.formatResult(mem).replace(",", "")
        onDigitClick(formatted)
    }

    fun onMemoryAdd() {
        val current = _state.value
        val expr = if (current.expression.isNotEmpty()) current.expression else "0"
        when (val res = ExpressionEvaluator.evaluate(expr, current.angleMode)) {
            is ExpressionEvaluator.EvalResult.Success -> {
                val newMem = current.memoryValue + res.value
                _state.update { it.copy(memoryValue = newMem, hasMemory = true) }
            }
            else -> {}
        }
    }

    fun onMemorySubtract() {
        val current = _state.value
        val expr = if (current.expression.isNotEmpty()) current.expression else "0"
        when (val res = ExpressionEvaluator.evaluate(expr, current.angleMode)) {
            is ExpressionEvaluator.EvalResult.Success -> {
                val newMem = current.memoryValue - res.value
                _state.update { it.copy(memoryValue = newMem, hasMemory = true) }
            }
            else -> {}
        }
    }

    private fun updatePreview() {
        val expr = _state.value.expression
        if (expr.isBlank()) {
            _state.update { it.copy(previewResult = "", error = null) }
            return
        }

        // Only preview if expression contains an operator or function
        val hasOp = expr.any { it in "+−-×*÷/%^√!sctlaeπ" }
        if (!hasOp) {
            _state.update { it.copy(previewResult = "", error = null) }
            return
        }

        // Try evaluating with auto-closed parentheses if necessary
        var testExpr = expr
        val openCount = testExpr.count { it == '(' }
        val closeCount = testExpr.count { it == ')' }
        if (openCount > closeCount) {
            testExpr += ")".repeat(openCount - closeCount)
        }

        when (val res = ExpressionEvaluator.evaluate(testExpr, _state.value.angleMode)) {
            is ExpressionEvaluator.EvalResult.Success -> {
                _state.update { it.copy(previewResult = "= ${res.formatted}", error = null) }
            }
            is ExpressionEvaluator.EvalResult.Error -> {
                _state.update { it.copy(previewResult = "") }
            }
        }
    }
}
