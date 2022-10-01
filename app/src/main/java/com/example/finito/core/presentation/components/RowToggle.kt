package com.example.finito.core.presentation.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun RowToggle(
    showContent: Boolean,
    onShowContentToggle: () -> Unit,
    label: String,
    @StringRes showContentDescription: Int,
    @StringRes hideContentDescription: Int,
    modifier: Modifier = Modifier
) {
    val rotate: Float by animateFloatAsState(if (showContent) 0f else -180f)
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = onShowContentToggle
            )
            .padding(16.dp),
    ) {
        Text(text = label)
        Icon(
            imageVector = Icons.Outlined.ExpandLess,
            contentDescription = stringResource(
                id = if (showContent)
                    hideContentDescription
                else
                    showContentDescription
            ),
            modifier = Modifier.rotate(rotate)
        )
    }
}