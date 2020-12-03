package cli

import cli.ActionResolver.Result.InvalidModifierArgsError
import cli.ActionResolver.Result.NoContentError
import cli.ActionResolver.Result.ParsingFailure
import cli.ActionResolver.Result.Resolved
import cli.ActionResolver.Result.UnknownCommandError
import cli.ActionResolver.Result.UnknownModifierError
import config.Argument
import config.Command
import config.Modifier
import config.ModifierValues
import util.empty
import util.withoutModSymbols

internal object ActionResolver {

  sealed class Result {

    data class Resolved(
      val command: Command,
      val modifierValues: ModifierValues = emptyMap(),
      val content: String,
    ) : Result()

    data class NoContentError(
      val command: Command,
      val modifierValues: ModifierValues = emptyMap(),
    ) : Result()

    data class InvalidModifierArgsError(
      val command: Command,
      val modifier: Modifier,
      val allArgs: List<String> = emptyList(),
    ) : Result()

    data class UnknownModifierError(
      val command: Command,
      val modifier: String,
    ) : Result()

    data class UnknownCommandError(
      val command: String,
    ) : Result()

    object ParsingFailure : Result()

  }

  fun resolve(args: Array<String>): Result {
    if (
      args.isEmpty() ||
      args.first().isBlank() ||
      args.first().matchesCommand(Command.HELP) ||
      args.first().matchesModifier(Modifier.HELP)
    ) return Resolved(Command.HELP, content = String.empty)

    val command = args.first()
    val resolvedCommand = Command.firstByName(command) ?: return UnknownCommandError(command)
    val modifiersWithContent = args.toList().drop(1) // remove the command

    when {
      // just a single command sent without any content
      modifiersWithContent.isEmpty() -> return NoContentError(resolvedCommand)

      // help requested for a command
      modifiersWithContent.first().matchesModifier(Modifier.HELP) -> return Resolved(
        command = resolvedCommand,
        modifierValues = linkedMapOf(Modifier.HELP to emptyMap()),
        content = modifiersWithContent.drop(1).lastOrNull() ?: String.empty,
      )

      // it's not HELP modifier, so one command with content was sent
      modifiersWithContent.size == 1 -> return Resolved(
        command = resolvedCommand,
        content = modifiersWithContent.first(),
      )
    }

    val resolvedModifierArgs = linkedMapOf<Modifier, LinkedHashMap<Argument, String>>()
    var currentModifier: Modifier? = null
    val getLastModifier = {
      currentModifier?.name
        ?: resolvedModifierArgs.keys.firstOrNull()?.name
        ?: modifiersWithContent.firstOrNull()
        ?: "?"
    }

    modifiersWithContent.forEachIndexed { i, item ->
      when {

        // looping: last item, we need to return a result
        i == modifiersWithContent.lastIndex -> {
          return when {
            // valid resolution
            currentModifier.areArgsValid(resolvedModifierArgs) -> Resolved(
              command = resolvedCommand,
              modifierValues = resolvedModifierArgs,
              content = item
            )
            // modifier args are not valid, and we know the modifier
            currentModifier != null -> InvalidModifierArgsError(
              command = resolvedCommand,
              modifier = currentModifier!!,
              allArgs = resolvedModifierArgs.mergePlainArgs()
            )
            // modifier args are not valid, but we don't know the modifier
            else -> UnknownModifierError(
              command = resolvedCommand,
              modifier = getLastModifier()
            )
          }
        }

        // looping: try to resolve modifiers
        item.startsWith(Modifier.SYMBOL) -> {
          val modifier = Modifier.firstByName(item) ?: return UnknownModifierError(resolvedCommand, item)

          when {
            // either this is a new modifier, previous modifiers were cleared, or
            // we already had a modifier before, with valid args
            currentModifier.areArgsValid(resolvedModifierArgs) -> {
              currentModifier = modifier
              resolvedModifierArgs[modifier] = linkedMapOf()
            }
            // we got a new modifier, but previous modifier was not cleared
            else -> return InvalidModifierArgsError(
              command = resolvedCommand,
              modifier = currentModifier!!,
              allArgs = resolvedModifierArgs.mergePlainArgs()
            )
          }
        }

        // looping: resolve modifier arguments
        else -> {
          val modifier = currentModifier ?: return InvalidModifierArgsError(
            command = resolvedCommand,
            modifier = Modifier.all.firstOrNull { it.name == getLastModifier() } ?: Modifier.HELP,
            allArgs = resolvedModifierArgs.mergePlainArgs() + item
          )
          val modifierArgs = resolvedModifierArgs.getOrPut(modifier) { linkedMapOf() }

          if (modifier.arguments.isEmpty() || modifierArgs.size == modifier.arguments.size) {
            // too many arguments for this modifier
            return InvalidModifierArgsError(
              command = resolvedCommand,
              modifier = modifier,
              allArgs = resolvedModifierArgs.mergePlainArgs() + item
            )
          }

          val nextArgument = modifier.arguments[modifierArgs.size]
          modifierArgs[nextArgument] = item

          if (modifierArgs.size == modifier.arguments.size) {
            // this was the last argument for the current modifier
            currentModifier = null
          }
        }
      }
    }

    return ParsingFailure
  }

  private fun String.matchesCommand(command: Command) = command.shortName == this || command.longName == this
  private fun String.matchesModifier(modifier: Modifier) = modifier.shortName == this.withoutModSymbols() ||
    modifier.longName == this.withoutModSymbols()

  private fun Modifier?.areArgsValid(modifierArgs: ModifierValues) = this == null ||
    modifierArgs[this].orEmpty().size == this.arguments.size

  private fun ModifierValues.mergePlainArgs(): List<String> =
    values
      .filterNot { it.isEmpty() }
      .fold(mutableListOf()) { bucket, argumentValue ->
        bucket.apply {
          addAll(argumentValue.values)
        }
      }

}