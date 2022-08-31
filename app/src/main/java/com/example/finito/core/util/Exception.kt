package com.example.finito.core.util

sealed class ResourceException(override val message: String?) : Exception(message) {
    object EmptyException : ResourceException(message = "Cannot be empty")

    class InvalidException(message: String?) : ResourceException(message)
}

object InvalidIdException : Exception("ID must be a positive integer")