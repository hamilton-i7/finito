package com.example.finito.core.util

import java.text.Normalizer

private val REGEX_NORMALIZE = "[^\\p{ASCII}]".toRegex()

fun CharSequence.normalize(): String {
    return Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .replace(REGEX_NORMALIZE, "")
        .lowercase()
}