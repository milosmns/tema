import chat.Chat
import cli.ActionResolver
import config.Command
import config.Modifier
import processing.ProcessorConfig

// Entry point

fun main(args: Array<String>) {

  when (val result = ActionResolver.resolve(args)) {

    ActionResolver.Result.ParsingFailure -> {
      Chat.printTotalFailure()
      Chat.printHelp()
    }

    is ActionResolver.Result.NoContentError -> {
      Chat.printNoContentError(result)
      Chat.printHelp()
    }

    is ActionResolver.Result.InvalidModifierArgsError -> {
      Chat.printInvalidArgsError(result)
      Chat.printHelpForModifier(
        modifier = result.modifier,
        parentCommand = result.command,
        simple = false,
        indent = 0
      )
    }

    is ActionResolver.Result.UnknownModifierError -> {
      Chat.printUnknownModifierError(result)
      Chat.printHelpForCommand(
        command = result.command,
        simple = false,
        indent = 0
      )
    }

    is ActionResolver.Result.UnknownCommandError -> {
      Chat.printUnknownCommandError(result)
      Chat.printHelp()
    }

    is ActionResolver.Result.Resolved -> {
      when {
        result.command == Command.HELP -> Chat.printHelp()
        result.modifierValues.keys.contains(Modifier.HELP) -> Chat.printHelpForCommand(
          command = result.command,
          simple = false,
          indent = 0
        )
        else -> {
          ProcessorConfig.getProcessorForCommand(result.command)
            ?.let {
              try {
                it.process(
                  modifierValues = result.modifierValues,
                  content = result.content,
                )
              } catch (error: Throwable) {
                error.message
              }
            }
            ?.also { println(it) }
            ?: Chat.printCommandMisconfiguration(result.command)
        }
      }
    }

  }

}
