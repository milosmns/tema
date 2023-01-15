package processing

import config.ModifierValues

internal interface Processor {

  @Throws(IllegalArgumentException::class)
  fun process(modifierValues: ModifierValues, content: String): String

}
