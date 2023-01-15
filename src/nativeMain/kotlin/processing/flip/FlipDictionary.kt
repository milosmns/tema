package processing.flip

internal object FlipDictionary {

  private val mappings = linkedMapOf(
    'a' to 'ɐ', // U+0250
    'b' to 'q',
    'c' to 'ɔ', // U+0254
    'd' to 'p',
    'e' to 'ǝ', // U+01DD
    'f' to 'ɟ', // U+025F
    'g' to 'ƃ', // U+0183
    'h' to 'ɥ', // U+0265
    'i' to 'ᴉ', // U+1D09
    'j' to 'ɾ', // U+027E
    'k' to 'ʞ', // U+029E
    'l' to 'l',
    'm' to 'ɯ', // U+026F
    'n' to 'u',
    'o' to 'o',
    'p' to 'd',
    'q' to 'b',
    'r' to 'ɹ', // U+0279
    's' to 's',
    't' to 'ʇ', // U+0287
    'u' to 'n',
    'v' to 'ʌ', // U+028C
    'w' to 'ʍ', // U+028D
    'x' to 'x',
    'y' to 'ʎ', // U+028E
    'z' to 'z',
    'A' to '∀', // U+2200
    'B' to 'B',
    'C' to 'Ɔ', // U+0186
    'D' to 'D',
    'E' to 'Ǝ', // U+018E
    'F' to 'Ⅎ', // U+2132
    'G' to 'פ', // U+05E4
    'H' to 'H',
    'I' to 'I',
    'J' to 'ſ', // U+017F
    'K' to 'K',
    'L' to '˥', // U+02E5
    'M' to 'W',
    'N' to 'N',
    'O' to 'O',
    'P' to 'Ԁ', // U+0500
    'Q' to 'Q',
    'R' to 'R',
    'S' to 'S',
    'T' to '┴', // U+2534
    'U' to '∩', // U+2229
    'V' to 'Λ', // U+039B
    'W' to 'M',
    'X' to 'X',
    'Y' to '⅄', // U+2144
    'Z' to 'Z',
    '0' to '0',
    '1' to 'Ɩ', // U+0196
    '2' to 'ᄅ', // U+1105
    '3' to 'Ɛ', // U+0190
    '4' to 'ㄣ', // U+3123
    '5' to 'ϛ', // U+03DB
    '6' to '9',
    '7' to 'ㄥ', // U+3125
    '8' to '8',
    '9' to '6',
    ',' to '\'',
    '.' to '˙', // U+02D9
    '?' to '¿', // U+00BF
    '!' to '¡', // U+00A1
    '"' to '„', // U+201E
    '\'' to ',',
    '`' to ',',
    '(' to ')',
    ')' to '(',
    '[' to ']',
    ']' to '[',
    '{' to '}',
    '}' to '{',
    '<' to '>',
    '>' to '<',
    '&' to '⅋', // U+214B
    '_' to '‾', // U+203E
  )

  fun flip(char: Char): Char = mappings[char] ?: char

}
