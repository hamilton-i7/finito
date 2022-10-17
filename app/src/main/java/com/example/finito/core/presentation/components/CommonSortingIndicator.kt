package com.example.finito.core.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CommonSortingIndicator(
    option: SortingOption.Common,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f)
    val contentColor by animateColorAsState(
        targetValue = if (isPressed) finitoColors.outline else finitoColors.onSurface
    )
    val scope = rememberCoroutineScope()

    Surface(contentColor = contentColor) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        scope.launch {
                            val press = PressInteraction.Press(Offset.Zero)
                            interactionSource.emit(press)
                            delay(120L)
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                        onClick()
                    }
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.up_down_arrow),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(id = option.label),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}