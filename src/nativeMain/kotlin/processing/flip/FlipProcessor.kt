package processing.flip

import processing.Processor
import processing.common.PaddingProcessor
import config.Modifier
import config.Modifier.HELP
import config.Modifier.PADDED
import config.ModifierValues
import util.empty

internal object FlipProcessor : Processor {

  @Throws(IllegalArgumentException::class)
  override fun process(modifierValues: ModifierValues, content: String): String {
    val args = validateModifiers(modifierValues)
    val result = content.flip()
    return processModifiers(modifierValues, args, result)
  }

  private fun String.flip(): String {
    if (this.isEmpty()) return String.empty

    val result = StringBuilder(length)
    forEach { char ->
      result.append(FlipDictionary.flip(char))
    }
    return result.toString()
  }

  private fun processModifiers(
    modifierValues: ModifierValues,
    args: Map<Modifier, FlipArgs>,
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
  private fun validateModifiers(modifierValues: ModifierValues): Map<Modifier, FlipArgs> {
    val args = linkedMapOf<Modifier, FlipArgs>()

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
