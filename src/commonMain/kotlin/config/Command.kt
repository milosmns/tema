package config

internal enum class Command(
  val longName: String,
  val shortName: String,
  val description: String,
) {

  HELP(
    longName = "help",
    shortName = "h",
    description = "Prints this help page",
  ),

  REVERSE(
    longName = "reverse",
    shortName = "r",
    description = "Reverses the given content",
  ),

  FLIP(
    longName = "flip",
    shortName = "f",
    description = "Flips the given content upside-down",
  ),

  ;

  companion object {

    val all = values().asList()

    fun firstByName(text: String) = all.firstOrNull { it.shortName == text || it.longName == text }

  }

}
