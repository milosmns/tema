package cli

import cli.ActionResolver.Result
import config.Argument
import config.Command
import config.Modifier
import util.empty
import util.space
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ActionResolverTest {

  // region Help

  @Test fun `command: help, short`() {
    val result = ActionResolver.resolve(
      "tema h"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.HELP,
        modifierValues = emptyMap(),
        content = String.empty,
      ),
      actual = result
    )
  }

  @Test fun `command: help, long`() {
    val result = ActionResolver.resolve(
      "tema help"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.HELP,
        modifierValues = emptyMap(),
        content = String.empty,
      ),
      actual = result
    )
  }

  @Test fun `modifier: help, short`() {
    val result = ActionResolver.resolve(
      "tema -h"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.HELP,
        modifierValues = emptyMap(),
        content = String.empty,
      ),
      actual = result
    )
  }

  @Test fun `modifier: help, long`() {
    val result = ActionResolver.resolve(
      "tema --help"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.HELP,
        modifierValues = emptyMap(),
        content = String.empty,
      ),
      actual = result
    )
  }

  // endregion

  // region Reverse

  @Test fun `command: reverse, short`() {
    val result = ActionResolver.resolve(
      "tema r abc"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.REVERSE,
        modifierValues = emptyMap(),
        content = "abc",
      ),
      actual = result
    )
  }

  @Test fun `command: reverse, long`() {
    val result = ActionResolver.resolve(
      "tema reverse abc"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.REVERSE,
        modifierValues = emptyMap(),
        content = "abc",
      ),
      actual = result
    )
  }

  @Test fun `command: reverse, short | modifier: help, short`() {
    val result = ActionResolver.resolve(
      "tema r -h"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.REVERSE,
        modifierValues = linkedMapOf(Modifier.HELP to emptyMap()),
        content = String.empty,
      ),
      actual = result
    )
  }

  @Test fun `command: reverse, short | modifier: help, long`() {
    val result = ActionResolver.resolve(
      "tema r --help"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.REVERSE,
        modifierValues = linkedMapOf(Modifier.HELP to emptyMap()),
        content = String.empty,
      ),
      actual = result
    )
  }

  @Test fun `command: reverse, long | modifier: help, short`() {
    val result = ActionResolver.resolve(
      "tema reverse -h"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.REVERSE,
        modifierValues = linkedMapOf(Modifier.HELP to emptyMap()),
        content = String.empty,
      ),
      actual = result
    )
  }

  @Test fun `command: reverse, long | modifier: help, long`() {
    val result = ActionResolver.resolve(
      "tema reverse --help"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.REVERSE,
        modifierValues = linkedMapOf(Modifier.HELP to emptyMap()),
        content = String.empty,
      ),
      actual = result
    )
  }

  @Test fun `command: reverse, short | modifier: padded, short`() {
    val result = ActionResolver.resolve(
      "tema r -p 4 * abc"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.REVERSE,
        modifierValues = linkedMapOf(Modifier.PADDED to linkedMapOf(Argument.TIMES to "4", Argument.PAD to "*")),
        content = "abc",
      ),
      actual = result
    )
  }

  @Test fun `command: reverse, long | modifier: padded, long`() {
    val result = ActionResolver.resolve(
      "tema reverse --padded 4 * abc"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.REVERSE,
        modifierValues = linkedMapOf(Modifier.PADDED to linkedMapOf(Argument.TIMES to "4", Argument.PAD to "*")),
        content = "abc",
      ),
      actual = result
    )
  }

  @Test fun `command: reverse, long | modifier: padded, long | modifier: help, long`() {
    val result = ActionResolver.resolve(
      "tema reverse --padded 4 * --help abc"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.REVERSE,
        modifierValues = linkedMapOf(
          Modifier.PADDED to linkedMapOf(Argument.TIMES to "4", Argument.PAD to "*"),
          Modifier.HELP to emptyMap(),
        ),
        content = "abc",
      ),
      actual = result
    )
  }

  // endregion

  // region Problems

  @Test fun `no args`() {
    val result = ActionResolver.resolve(
      "tema"
    )

    assertEquals(
      expected = Result.Resolved(
        command = Command.HELP,
        modifierValues = emptyMap(),
        content = String.empty,
      ),
      actual = result
    )
  }

  @Test fun `invalid command`() {
    val result = ActionResolver.resolve(
      "tema invalid_command abc"
    )

    assertEquals(
      expected = Result.UnknownCommandError(command = "invalid_command"),
      actual = result
    )
  }

  @Test fun `invalid modifier`() {
    val result = ActionResolver.resolve(
      "tema reverse --invalid_modifier arg abc"
    )

    assertEquals(
      expected = Result.UnknownModifierError(
        command = Command.REVERSE,
        modifier = "--invalid_modifier",
      ),
      actual = result
    )
  }

  @Test fun `no content | no modifiers`() {
    val result = ActionResolver.resolve(
      "tema reverse"
    )

    assertEquals(
      expected = Result.NoContentError(
        command = Command.REVERSE,
        modifierValues = emptyMap(),
      ),
      actual = result
    )
  }

  @Test fun `invalid modifier args, too few | modifier: padded, long`() {
    val result = ActionResolver.resolve(
      "tema reverse --padded 4 *"
    )

    assertEquals(
      expected = Result.InvalidModifierArgsError(
        command = Command.REVERSE,
        modifier = Modifier.PADDED,
        allArgs = listOf("4"), // '*' treated as content
      ),
      actual = result
    )
  }

  @Test fun `invalid modifier args, too many | modifier: padded, long`() {
    val result = ActionResolver.resolve(
      "tema reverse --padded 4 * x abc"
    )

    assertEquals(
      expected = Result.InvalidModifierArgsError(
        command = Command.REVERSE,
        modifier = Modifier.PADDED,
        allArgs = listOf("4", "*", "x"),
      ),
      actual = result
    )
  }

  @Test fun `invalid modifier args | modifier: padded, long | modifier: help, long`() {
    val result = ActionResolver.resolve(
      "tema reverse --padded 4 * --help x abc"
    )

    assertEquals(
      expected = Result.InvalidModifierArgsError(
        command = Command.REVERSE,
        modifier = Modifier.HELP,
        allArgs = listOf("4", "*", "x"),
      ),
      actual = result
    )
  }

  // endregion

  // Helpers

  private fun ActionResolver.resolve(cmd: String) = resolve(
    cmd.replaceFirst("tema", "")
      .trim()
      .split(String.space.first())
      .toTypedArray()
  )

}