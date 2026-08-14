package com.example.calculator.model

data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AngleMode {
    DEG, RAD
}

data class CalculatorState(
    val expression: String = "",
    val previewResult: String = "",
    val finalResult: String = "",
    val error: String? = null,
    val isScientificExpanded: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false,
    val history: List<HistoryItem> = emptyList(),
    val showHistory: Boolean = false
)
