package util

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ExtensionsTest {

  @Test fun `modifier symbols removal - single symbol`() {
    val withModifier = "-help"
    val result = withModifier.withoutModSymbols()
    assertEquals(expected = "help", actual = result)
  }

  @Test fun `modifier symbols removal - multiple symbols`() {
    val withModifier = "--help"
    val result = withModifier.withoutModSymbols()
    assertEquals(expected = "help", actual = result)
  }

}
