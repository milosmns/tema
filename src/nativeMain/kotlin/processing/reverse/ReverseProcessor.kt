package processing.reverse

import processing.Processor
import processing.common.PaddingProcessor
import config.Modifier
import config.Modifier.HELP
import config.Modifier.PADDED
import config.ModifierValues
import util.empty

internal object ReverseProcessor : Processor {

  @Throws(IllegalArgumentException::class)
  override fun process(modifierValues: ModifierValues, content: String): String {
    val args = validateModifiers(modifierValues)
    val result = content.reverse()
    return processModifiers(modifierValues, args, result)
  }

  private fun String.reverse(): String {
    if (this.isEmpty()) return String.empty

    return this.reversed() // Kotlin's stdlib
  }

  private fun processModifiers(
    modifierValues: ModifierValues,
    args: Map<Modifier, ReverseArgs>,
    content: String,
  ): String {
    var result = content
    modifierValues.keys.forEach {
      when (it) {
        PADDED -> result = PaddingProcessor.process(args[it] as PaddingProcessor.Args, content)
        HELP -> it.error()
      }
    }
    return result
  }

  @Throws(IllegalArgumentException::class)
  private fun validateModifiers(modifierValues: ModifierValues): Map<Modifier, ReverseArgs> {
    val args = linkedMapOf<Modifier, ReverseArgs>()

    modifierValues.forEach { (modifier, arguments) ->
      when (modifier) {
        PADDED -> args[modifier] = PaddingProcessor.validateArgs(arguments)
        else -> modifier.error()
      }
    }

    return args
  }

  private fun Modifier.error(): Nothing = error("$longName is not supported with this command")

}
