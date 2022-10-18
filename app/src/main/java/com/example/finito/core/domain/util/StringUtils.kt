package com.example.finito.core.domain.util

import java.text.Normalizer

private val REGEX_NORMALIZE = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun CharSequence.normalize(): String {
    return Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .replace(REGEX_NORMALIZE, "")
        .lowercase()
}

fun Char.normalize(): Char {
    return Normalizer
        .normalize(toString(), Normalizer.Form.NFD)
        .replace(REGEX_NORMALIZE, "")[0]
        .lowercaseChar()
}