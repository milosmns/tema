package processing.common

import processing.flip.FlipArgs
import processing.reverse.ReverseArgs
import config.Argument
import config.ArgumentValues

internal object PaddingProcessor {

  data class Args(
    val times: Int,
    val pad: String,
  ) : ReverseArgs, FlipArgs

  fun process(args: Args, content: String) = with(args) { "${pad.repeat(times)}$content${pad.repeat(times)}" }

  @Throws(IllegalArgumentException::class)
  fun validateArgs(allArgs: ArgumentValues): Args {
    val timesRaw = allArgs[Argument.TIMES]
    val padRaw = allArgs[Argument.PAD]
    val times = timesRaw?.toIntOrNull() ?: throw IllegalArgumentException("'$timesRaw' is not a valid number")
    val pad = padRaw?.takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("Empty pad is not allowed")
    return Args(times = times, pad = pad)
  }

}
