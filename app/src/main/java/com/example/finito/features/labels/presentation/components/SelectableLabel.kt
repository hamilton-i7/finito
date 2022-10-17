package com.example.finito.features.labels.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors

@Composable
fun SelectableLabel(
    label: SimpleLabel,
    selected: Boolean = false,
) {
    Surface(
        shape = CircleShape,
        border = BorderStroke(width = 2.dp, color = finitoColors.primary),
        color = if (selected) finitoColors.primary else finitoColors.surface,
        modifier = Modifier.wrapContentSize()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
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