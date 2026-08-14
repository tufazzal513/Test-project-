package com.example

import com.example.calculator.engine.ExpressionEvaluator
import com.example.calculator.model.AngleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    val res = ExpressionEvaluator.evaluate("2 + 2", AngleMode.DEG)
    assertTrue(res is ExpressionEvaluator.EvalResult.Success)
    assertEquals("4", (res as ExpressionEvaluator.EvalResult.Success).formatted)
  }

  @Test
  fun operatorPrecedence_isCorrect() {
    val res = ExpressionEvaluator.evaluate("2 + 3 * 4", AngleMode.DEG)
    assertTrue(res is ExpressionEvaluator.EvalResult.Success)
    assertEquals("14", (res as ExpressionEvaluator.EvalResult.Success).formatted)
  }

  @Test
  fun scientificTrigonometry_isCorrect() {
    val res = ExpressionEvaluator.evaluate("sin(90)", AngleMode.DEG)
    assertTrue(res is ExpressionEvaluator.EvalResult.Success)
    assertEquals("1", (res as ExpressionEvaluator.EvalResult.Success).formatted)
  }

  @Test
  fun divisionByZero_handledGracefully() {
    val res = ExpressionEvaluator.evaluate("10 / 0", AngleMode.DEG)
    assertTrue(res is ExpressionEvaluator.EvalResult.Error)
  }
}

