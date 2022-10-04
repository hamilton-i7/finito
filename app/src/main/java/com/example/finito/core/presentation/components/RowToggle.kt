package com.example.finito.core.presentation.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.ui.theme.DisabledAlpha
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowToggle(
    showContent: Boolean,
    onShowContentToggle: () -> Unit,
    label: String,
    @StringRes showContentDescription: Int,
    @StringRes hideContentDescription: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val rotate: Float by animateFloatAsState(if (showContent) 0f else -180f)

    Surface(
        onClick = onShowContentToggle,
        enabled = enabled,
        contentColor = if (enabled) finitoColors.onSurface
        else finitoColors.onSurface.copy(alpha = DisabledAlpha),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
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
}