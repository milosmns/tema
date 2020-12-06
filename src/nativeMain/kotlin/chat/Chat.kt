package chat

import cli.ActionResolver.Result.InvalidModifierArgsError
import cli.ActionResolver.Result.NoContentError
import cli.ActionResolver.Result.UnknownCommandError
import cli.ActionResolver.Result.UnknownModifierError
import config.Argument
import config.Command
import config.Modifier
import util.space

internal object Chat {

  // region Help pages

  fun printHelp() {
    "This is the Text Manipulator (TEMA). The basic usage is as follows:\n".pwi()
    "tema <operation> [modifiers] <content>\n".pwi(1)
    "Supported operations are:\n".pwi()
    Command.all.forEach { printHelpForCommand(it, simple = true, indent = 1) }.ln()
    "For help about any operation, use the --help modifier after it.".pwi()
  }

  fun printHelpForCommand(
    command: Command,
    simple: Boolean,
    indent: Int,
  ) = when {
    simple -> "[${command.shortName}] ${command.longName} : ${command.description}".pwi(indent)
    else -> {
      "Operation '${command.longName}' can be used as follows:\n".pwi(indent)
      "tema ${command.longName} [modifiers] <content>".pwi(indent + 1)
      "or".pwi(indent)
      "tema ${command.shortName} [modifiers] <content>\n".pwi(indent + 1)
      "${command.description}.\n".pwi(indent)

      Modifier.allMatchingCommand(command)
        .takeIf { it.isNotEmpty() }
        ?.let { modifiers ->
          "Modifiers usable with this operation are:\n".pwi(indent)
          modifiers.forEach {
            printHelpForModifier(it, parentCommand = command, simple = true, indent = indent + 1)
          }.ln()
          "For help about any modifier, use it without any arguments.".pwi(indent)
        }
    }
  }

  fun printHelpForModifier(
    modifier: Modifier,
    parentCommand: Command? = null,
    simple: Boolean,
    indent: Int,
  ) = when {
    simple -> ("[${modifier.shortName}] ${modifier.longName} : " +
      "${modifier.description} (${modifier.arguments.size} args)").pwi(indent)
    else -> {
      val argsText = List(modifier.arguments.size) { "arg${it + 1}" }.joinToString(String.space)

      "Modifier '${modifier.longName}' can be used as follows:\n".pwi(indent)
      ("tema ${parentCommand?.longName ?: "<operation>"} " +
        "${Modifier.SYMBOL.repeat(2)}${modifier.longName} $argsText").pwi(indent + 1)
      "or".pwi(indent)
      ("tema ${parentCommand?.longName ?: "<operation>"} " +
        "${Modifier.SYMBOL}${modifier.shortName} $argsText\n").pwi(indent + 1)
      "${modifier.description}.\n".pwi(indent)

      modifier.arguments
        .takeIf { it.isNotEmpty() }
        ?.let { args ->
          "Arguments usable with this operation are:\n".pwi(indent)
          args.forEach { printHelpForArg(it, indent = indent + 1) }
        }
    }
  }

  fun printHelpForArg(
    argument: Argument,
    indent: Int,
  ) = "${argument.longName} : ${argument.description}".pwi(indent)

  // endregion

  // region Errors

  fun printTotalFailure() = "\nFatal error, forced to stop.".pwi().ln()

  fun printNoContentError(error: NoContentError) = ("\nNo content provided for operation '${error.command.longName}'.\n" +
    "Make sure that content is provided as the last parameter, " +
    "and use quotations (\") if your content includes spaces.").pwi().ln()

  fun printInvalidArgsError(error: InvalidModifierArgsError) = ("\nInvalid arguments for modifier " +
    "'${error.modifier.longName}' on operation '${error.command.longName}'.\n" +
    "Arguments provided: [${error.allArgs.joinToString()}].").pwi().ln()

  fun printUnknownModifierError(error: UnknownModifierError) = ("\nUnknown modifier '${error.modifier}' " +
    "for operation '${error.command.longName}'.").pwi().ln()

  fun printUnknownCommandError(error: UnknownCommandError) = "\nUnknown command '${error.command}'.".pwi().ln()

  // endregion

  // PWI - print with indent
  private fun String.pwi(indentSize: Int = 0) = println("${"\t".repeat(indentSize)}$this")
  @Suppress("unused") private fun Unit.ln() = println()

}