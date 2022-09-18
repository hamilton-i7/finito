package com.example.finito.core.domain

sealed class Result<out T, out E> {
    data class Success<T>(val data: T) : Result<T, Nothing>()

    data class Error(val message: String) : Result<Nothing, String>()
}
