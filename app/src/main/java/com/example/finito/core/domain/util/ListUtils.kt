package com.example.finito.core.domain.util

fun <T> List<T>.moveElement(start: Int, end: Int): List<T> = toMutableList().apply {
    add(end, removeAt(start))
}

fun <T> List<T>.takeRandom(): List<T> {
    if (isEmpty()) return emptyList()
    return take((0..size).random())
}