package util

import config.Modifier

internal val String.Companion.space: String get() = " "
internal val String.Companion.empty: String get() = ""

internal fun String.withoutModSymbols() = replace(Modifier.SYMBOL, "")
