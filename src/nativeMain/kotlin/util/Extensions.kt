package util

import config.Modifier

val String.Companion.space: String get() = " "
val String.Companion.empty: String get() = ""

fun String.withoutModSymbols() = replace(Modifier.SYMBOL, "")
