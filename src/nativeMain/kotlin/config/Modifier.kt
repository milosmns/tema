package config

import config.Argument.PAD
import config.Argument.TIMES
import util.withoutModSymbols

typealias ModifierValues = Map<Modifier, ArgumentValues>

enum class Modifier(
  val longName: String,
  val shortName: String,
  val description: String,
  val supportedInCommands: List<Command>,
  val arguments: List<Argument>,
) {

  HELP(
    longName = "help",
    shortName = "h",
    description = "Shows the help page for the given command",
    supportedInCommands = Command.all,
    arguments = emptyList(),
  ),

  PADDED(
    longName = "padded",
    shortName = "p",
    description = "Pads the output from left and right using the given character",
    supportedInCommands = Command.all,
    arguments = listOf(TIMES, PAD),
  )

  ;

  companion object {

    const val SYMBOL = "-"
    val all = values().asList()

    fun allMatchingCommand(command: Command) = all.filter { command in it.supportedInCommands }

    fun firstByName(text: String) = all.firstOrNull {
      val clearText = text.withoutModSymbols()
      it.shortName == clearText || it.longName == clearText
    }

  }

}