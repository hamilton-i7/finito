package com.example.finito.core.presentation.util

data class TextFieldState(
    val value: String = "",
    val onValueChange: (String) -> Unit = {},
)
