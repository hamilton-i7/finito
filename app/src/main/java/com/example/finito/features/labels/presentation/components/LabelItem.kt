package com.example.finito.features.labels.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.example.finito.R
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.DisabledAlpha
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelItem(
    label: SimpleLabel,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onLabelClick: () -> Unit = {},
) {
    val textColor = if (enabled) finitoColors.onSurface else finitoColors.onSurface.copy(
        alpha = DisabledAlpha
    )

    ListItem(
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.tag_window),
                contentDescription = null,
                tint = textColor,
            )
        },
        headlineText = {
            Text(
                text = label.name,
                color = textColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        trailingContent = {
            Checkbox(
                checked = selected,
                enabled = enabled,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = finitoColors.tertiary
                )
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = selected,
                role = Role.Checkbox,
                onValueChange = { onLabelClick() }
            )
            .then(modifier)
    )
}