package com.example.finito.core.util

@Throws(InvalidIdException::class)
fun isValidId(id: Int): Boolean {
    if (id <= 0) {
        throw InvalidIdException
    }
    return true
}