package com.example.finito.core.domain

sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()

    data class Error(val message: String) : Result<Unit>()
}
