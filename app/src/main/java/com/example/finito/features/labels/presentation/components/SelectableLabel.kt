package com.example.finito.features.labels.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableLabel(
    label: SimpleLabel,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val size = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> 150.dp
        else -> 120.dp
    }
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        shape = CircleShape,
        border = if (selected)
            BorderStroke(width = 2.dp, color = finitoColors.tertiary)
        else
            null,
        color = finitoColors.surfaceColorAtElevation(12.dp),
        modifier = Modifier
            .wrapContentSize()
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                role = Role.Button,
                indication = rememberRipple(),
                interactionSource = interactionSource
            )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.tag_window),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SelectableLabelPreview() {
    FinitoTheme {
        Surface {
            SelectableLabel(label = SimpleLabel.dummyLabels.random())
        }
    }
}

@ThemePreviews
@Composable
private fun SelectableLabelSelectedPreview() {
    FinitoTheme {
        Surface {
            SelectableLabel(
                label = SimpleLabel.dummyLabels.random(),
                selected = true
            )
        }
    }
}