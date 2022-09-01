package com.example.finito.core.util

fun <T>List<T>.moveElement(start: Int, end: Int): List<T> {
    val result = this.toMutableList()

    val removed = result.removeAt(start)
    result.add(end, removed)

    return result
}