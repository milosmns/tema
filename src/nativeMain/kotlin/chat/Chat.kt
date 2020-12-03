package chat

import config.Command

internal object Chat {

  fun printHelp() {
    println("This is the Text Manipulator (TEMA). The basic usage is as follows:\n")
    println("\t tema <command> [modifiers] [content]\n")
    println("Supported commands are:\n")
    println(
      Command.all.sorted().joinToString("\n") {
        // help [h] : Prints this help page
        "\t [${it.shortName}] ${it.longName} : ${it.description}"
      }
    )
  }

}