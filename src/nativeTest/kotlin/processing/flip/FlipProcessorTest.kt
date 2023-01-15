package processing.flip

import processing.Processor
import config.Argument
import config.Modifier
import config.ModifierValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class FlipProcessorTest {

  private val processor: Processor = FlipProcessor

  @Test fun `process empty`() {
    val modifiers: ModifierValues = emptyMap()

    val result = processor.process(modifiers, "")

    assertEquals(
      expected = "",
      actual = result,
    )
  }

  @Test fun `process non empty - no modifiers`() {
    val modifiers: ModifierValues = emptyMap()

    val result = processor.process(modifiers, "test1")

    assertEquals(
      expected = "ʇǝsʇƖ",
      actual = result,
    )
  }

  @Test fun `process non empty - invalid modifier`() {
    val modifiers: ModifierValues = linkedMapOf(
      Modifier.HELP to emptyMap(),
    )

    assertFailsWith(IllegalStateException::class) {
      processor.process(modifiers, "test1")
    }
  }

  @Test fun `process non empty - invalid argument`() {
    val modifiers: ModifierValues = linkedMapOf(
      Modifier.PADDED to linkedMapOf(
        Argument.TIMES to "A", // invalid
        Argument.PAD to "-",
      ),
    )

    assertFailsWith(IllegalArgumentException::class) {
      processor.process(modifiers, "test1")
    }
  }

  @Test fun `process non empty - with padding`() {
    val modifiers: ModifierValues = linkedMapOf(
      Modifier.PADDED to linkedMapOf(
        Argument.TIMES to "2",
        Argument.PAD to "-",
      ),
    )

    val result = processor.process(modifiers, "test1")

    assertEquals(
      expected = "--ʇǝsʇƖ--",
      actual = result,
    )
  }

}
