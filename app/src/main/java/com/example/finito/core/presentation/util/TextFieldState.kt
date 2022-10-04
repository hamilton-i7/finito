package com.example.finito.core.presentation.util

data class TextFieldState(
    val value: String = "",
    val onValueChange: (String) -> Unit = {},
) {
    companion object {
        val Default = TextFieldState()
    }
}

data class SubtaskTextField(
    val id: Int,
    val completed: Boolean = false,
    val value: String = "",
    val onValueChange: (String) -> Unit = {},
) {
    fun toTextFieldState(): TextFieldState = TextFieldState(value, onValueChange)
}
