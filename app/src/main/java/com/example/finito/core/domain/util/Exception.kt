package com.example.finito.core.domain.util

sealed class ResourceException(override val message: String?) : Exception(message) {
    object EmptyException : ResourceException(message = "Cannot be empty")

    class InvalidStateException(message: String?) : ResourceException(message)

    object NegativeIdException : Exception("ID must be a positive integer")

    object NotFoundException : ResourceException(message = "Resource not found")
}