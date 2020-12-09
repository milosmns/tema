package processing

import config.Command
import processing.flip.FlipProcessor
import processing.reverse.ReverseProcessor

internal object ProcessorConfig {

  private val mappings = mapOf(
    Command.REVERSE to ReverseProcessor,
    Command.FLIP to FlipProcessor,
  )

  fun getProcessorForCommand(command: Command) = mappings[command]

}