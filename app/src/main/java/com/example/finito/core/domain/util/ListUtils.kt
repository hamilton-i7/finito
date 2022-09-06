package com.example.finito.core.domain.util

fun <T> List<T>.moveElement(start: Int, end: Int): List<T> {
    val result = this.toMutableList()

    val removed = result.removeAt(start)
    result.add(end, removed)

    return result
}

fun <T> List<T>.takeRandom(): List<T> {
    if (isEmpty()) return emptyList()
    return take((0..size).random())
}