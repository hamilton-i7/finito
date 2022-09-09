package com.example.finito.core.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.finito.ui.theme.finitoColors

@Composable
fun FinitoDivider(modifier: Modifier = Modifier) {
    Divider(
        color = finitoColors.onPrimaryContainer.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth()
    )
}