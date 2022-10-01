package com.example.finito.core.presentation.util

data class TextFieldState(
    val id: Int = -1,
    val value: String = "",
    val onValueChange: (String) -> Unit = {},
)
