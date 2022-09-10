package com.example.finito.core.presentation.components.util

data class TextFieldState(
    val value: String = "",
    val onValueChange: (String) -> Unit = {},
    val error: Boolean = false
)
