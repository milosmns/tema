package processing.common

import config.Argument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class PaddingProcessorTest {

  private val processor = PaddingProcessor
  private val templateArgs = PaddingProcessor.Args(times = 2, pad = "-")

  @Test fun `validate missing arg - int`() {
    assertFailsWith(IllegalArgumentException::class) {
      processor.validateArgs(
        allArgs = linkedMapOf(
          Argument.PAD to "-",
        ),
      )
    }
  }

  @Test fun `validate missing arg - string`() {
    assertFailsWith(IllegalArgumentException::class) {
      processor.validateArgs(
        allArgs = linkedMapOf(
          Argument.TIMES to "2",
        ),
      )
    }
  }

  @Test fun `validate invalid arg format - int`() {
    assertFailsWith(IllegalArgumentException::class) {
      processor.validateArgs(
        allArgs = linkedMapOf(
          Argument.TIMES to "A",
          Argument.PAD to "-",
        ),
      )
    }
  }

  @Test fun `validate invalid arg format - string`() {
    assertFailsWith(IllegalArgumentException::class) {
      processor.validateArgs(
        allArgs = linkedMapOf(
          Argument.TIMES to "2",
          Argument.PAD to "",
        ),
      )
    }
  }

  @Test fun `validate valid args`() {
    val args = processor.validateArgs(
      allArgs = linkedMapOf(
        Argument.TIMES to "2",
        Argument.PAD to "-",
      ),
    )

    assertEquals(
      expected = templateArgs,
      actual = args,
    )
  }

  @Test fun `process empty content`() {
    val result = processor.process(
      args = templateArgs,
      content = "",
    )

    assertEquals(
      expected = "----",
      actual = result,
    )
  }

  @Test fun `process empty pad`() {
    val result = processor.process(
      args = templateArgs.copy(pad = ""),
      content = "test",
    )

    assertEquals(
      expected = "test",
      actual = result,
    )
  }

  @Test fun `process zero times`() {
    val result = processor.process(
      args = templateArgs.copy(times = 0),
      content = "test",
    )

    assertEquals(
      expected = "test",
      actual = result,
    )
  }

  @Test fun `process content`() {
    val result = processor.process(
      args = templateArgs,
      content = "test",
    )

    assertEquals(
      expected = "--test--",
      actual = result,
    )
  }

}