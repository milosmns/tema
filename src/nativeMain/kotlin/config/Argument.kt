package config

typealias ArgumentValues = Map<Argument, String>

enum class Argument(
  val longName: String,
  val description: String,
) {

  TIMES(
    longName = "times",
    description = "How many times to pad the output",
  ),

  PAD(
    longName = "pad",
    description = "Text to use for padding (space must be quoted)",
  )

  ;

  companion object {

    val all = values().asList()

    fun firstByName(text: String) = all.firstOrNull { it.longName == text }

  }

}