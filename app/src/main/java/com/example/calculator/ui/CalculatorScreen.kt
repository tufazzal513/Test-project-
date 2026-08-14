package com.example.calculator.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculatorViewModel
import com.example.calculator.model.AngleMode
import com.example.calculator.model.CalculatorState
import com.example.ui.theme.*

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Controls Bar
            TopBarControls(
                state = state,
                onToggleAngle = { viewModel.toggleAngleMode() },
                onToggleScientific = { viewModel.toggleScientific() },
                onToggleHistory = { viewModel.toggleHistory() },
                onMemoryClear = { viewModel.onMemoryClear() },
                onMemoryRecall = { viewModel.onMemoryRecall() },
                onMemoryAdd = { viewModel.onMemoryAdd() },
                onMemorySubtract = { viewModel.onMemorySubtract() }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Display Output Area with modern card layout
            CalculatorDisplay(
                state = state,
                onBackspace = { viewModel.onBackspaceClick() },
                onClear = { viewModel.onClearAll() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Scientific panel (collapsible with smooth animation)
            AnimatedVisibility(
                visible = state.isScientificExpanded,
                enter = expandVertically(spring(dampingRatio = 0.8f)) + fadeIn(),
                exit = shrinkVertically(spring(dampingRatio = 0.8f)) + fadeOut()
            ) {
                Column {
                    ScientificKeypad(
                        onFunction = { viewModel.onFunctionClick(it) },
                        onConstant = { viewModel.onConstantClick(it) },
                        onPower = { viewModel.onPowerClick() },
                        onSqrt = { viewModel.onSqrtClick() },
                        onFactorial = { viewModel.onFactorialClick() }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Main Keypad Grid
            StandardKeypad(
                state = state,
                onDigit = { viewModel.onDigitClick(it) },
                onOperator = { viewModel.onOperatorClick(it) },
                onDecimal = { viewModel.onDecimalClick() },
                onClearAll = { viewModel.onClearAll() },
                onBackspace = { viewModel.onBackspaceClick() },
                onToggleSign = { viewModel.onToggleSign() },
                onPercent = { viewModel.onPercentClick() },
                onParenthesis = { viewModel.onParenthesisClick() },
                onEquals = { viewModel.onEqualsClick() }
            )
        }

        if (state.showHistory) {
            HistorySheet(
                history = state.history,
                onItemClick = { viewModel.onHistoryItemClick(it) },
                onClearHistory = { viewModel.onClearHistory() },
                onDismiss = { viewModel.toggleHistory() }
            )
        }
    }
}

@Composable
private fun TopBarControls(
    state: CalculatorState,
    onToggleAngle: () -> Unit,
    onToggleScientific: () -> Unit,
    onToggleHistory: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Angle mode badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleAngle() }
                        .testTag("angle_mode_button")
                ) {
                    Text(
                        text = state.angleMode.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                // Memory pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SmallPillButton(text = "MC", enabled = state.hasMemory, onClick = onMemoryClear, testTag = "mem_clear_btn")
                    SmallPillButton(text = "MR", enabled = state.hasMemory, onClick = onMemoryRecall, testTag = "mem_recall_btn")
                    SmallPillButton(text = "M+", enabled = true, onClick = onMemoryAdd, testTag = "mem_add_btn")
                    SmallPillButton(text = "M−", enabled = true, onClick = onMemorySubtract, testTag = "mem_sub_btn")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Scientific mode toggle button
                IconButton(
                    onClick = onToggleScientific,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_scientific_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Toggle Scientific Keypad",
                        tint = if (state.isScientificExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // History button
                IconButton(
                    onClick = onToggleHistory,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("history_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (state.history.isNotEmpty()) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(state.history.size.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Calculation History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallPillButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun CalculatorDisplay(
    state: CalculatorState,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(state.expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Expression Scrollable Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.expression.isEmpty()) "0" else state.expression,
                    fontSize = if (state.expression.length > 12) 36.sp else 48.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = if (state.expression.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("calculator_expression_display")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preview Result or Error display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Backspace button inside display for quick editing
                if (state.expression.isNotEmpty()) {
                    IconButton(
                        onClick = onBackspace,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("display_backspace_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }

                if (state.error != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (state.previewResult.isNotEmpty()) {
                    Text(
                        text = state.previewResult,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("calculator_preview_display")
                    )
                }
            }
        }
    }
}

@Composable
private fun ScientificKeypad(
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onPower: () -> Unit,
    onSqrt: () -> Unit,
    onFactorial: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: sin, cos, tan, ln, log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScientificButton("sin", onClick = { onFunction("sin") }, modifier = Modifier.weight(1f))
                ScientificButton("cos", onClick = { onFunction("cos") }, modifier = Modifier.weight(1f))
                ScientificButton("tan", onClick = { onFunction("tan") }, modifier = Modifier.weight(1f))
                ScientificButton("ln", onClick = { onFunction("ln") }, modifier = Modifier.weight(1f))
                ScientificButton("log", onClick = { onFunction("log") }, modifier = Modifier.weight(1f))
            }

            // Row 2: √, xʸ, n!, π, e
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScientificButton("√", onClick = onSqrt, modifier = Modifier.weight(1f))
                ScientificButton("xʸ", onClick = onPower, modifier = Modifier.weight(1f))
                ScientificButton("n!", onClick = onFactorial, modifier = Modifier.weight(1f))
                ScientificButton("π", onClick = { onConstant("π") }, modifier = Modifier.weight(1f))
                ScientificButton("e", onClick = { onConstant("e") }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ScientificButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(),
        label = "sci_scale"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .height(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = ripple()) { onClick() }
            .testTag("sci_btn_$text")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StandardKeypad(
    state: CalculatorState,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal: () -> Unit,
    onClearAll: () -> Unit,
    onBackspace: () -> Unit,
    onToggleSign: () -> Unit,
    onPercent: () -> Unit,
    onParenthesis: () -> Unit,
    onEquals: () -> Unit
) {
    val spacing = 12.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Row 1: C, ( ), %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            KeypadButton(text = "C", type = ButtonType.Action, modifier = Modifier.weight(1f), onClick = onClearAll, testTag = "calc_btn_clear")
            KeypadButton(text = "( )", type = ButtonType.Action, modifier = Modifier.weight(1f), onClick = onParenthesis, testTag = "calc_btn_parenthesis")
            KeypadButton(text = "%", type = ButtonType.Action, modifier = Modifier.weight(1f), onClick = onPercent, testTag = "calc_btn_percent")
            KeypadButton(text = "÷", type = ButtonType.Operator, modifier = Modifier.weight(1f), onClick = { onOperator("÷") }, testTag = "calc_btn_div")
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            KeypadButton(text = "7", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("7") }, testTag = "calc_btn_7")
            KeypadButton(text = "8", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("8") }, testTag = "calc_btn_8")
            KeypadButton(text = "9", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("9") }, testTag = "calc_btn_9")
            KeypadButton(text = "×", type = ButtonType.Operator, modifier = Modifier.weight(1f), onClick = { onOperator("×") }, testTag = "calc_btn_mul")
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            KeypadButton(text = "4", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("4") }, testTag = "calc_btn_4")
            KeypadButton(text = "5", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("5") }, testTag = "calc_btn_5")
            KeypadButton(text = "6", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("6") }, testTag = "calc_btn_6")
            KeypadButton(text = "−", type = ButtonType.Operator, modifier = Modifier.weight(1f), onClick = { onOperator("−") }, testTag = "calc_btn_sub")
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            KeypadButton(text = "1", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("1") }, testTag = "calc_btn_1")
            KeypadButton(text = "2", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("2") }, testTag = "calc_btn_2")
            KeypadButton(text = "3", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("3") }, testTag = "calc_btn_3")
            KeypadButton(text = "+", type = ButtonType.Operator, modifier = Modifier.weight(1f), onClick = { onOperator("+") }, testTag = "calc_btn_add")
        }

        // Row 5: +/−, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            KeypadButton(text = "+/−", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = onToggleSign, testTag = "calc_btn_sign")
            KeypadButton(text = "0", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = { onDigit("0") }, testTag = "calc_btn_0")
            KeypadButton(text = ".", type = ButtonType.Number, modifier = Modifier.weight(1f), onClick = onDecimal, testTag = "calc_btn_dot")
            KeypadButton(text = "=", type = ButtonType.Equals, modifier = Modifier.weight(1f), onClick = onEquals, testTag = "calc_btn_equals")
        }
    }
}

enum class ButtonType {
    Number, Operator, Action, Equals
}

@Composable
private fun KeypadButton(
    text: String,
    type: ButtonType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "btn_scale_$text"
    )

    val containerColor = when (type) {
        ButtonType.Number -> MaterialTheme.colorScheme.surface
        ButtonType.Action -> MaterialTheme.colorScheme.surfaceVariant
        ButtonType.Operator -> MaterialTheme.colorScheme.primary
        ButtonType.Equals -> MaterialTheme.colorScheme.secondary
    }

    val contentColor = when (type) {
        ButtonType.Number -> MaterialTheme.colorScheme.onSurface
        ButtonType.Action -> MaterialTheme.colorScheme.onSurfaceVariant
        ButtonType.Operator -> Color.White
        ButtonType.Equals -> Color.White
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        tonalElevation = if (type == ButtonType.Number) 3.dp else 0.dp,
        shadowElevation = if (type == ButtonType.Equals || type == ButtonType.Operator) 4.dp else 1.dp,
        modifier = modifier
            .height(68.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = ripple()) { onClick() }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = if (text.length > 2) 20.sp else 28.sp,
                fontWeight = if (type == ButtonType.Number) FontWeight.Normal else FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
