package com.example.finito.features.labels.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelItem(
    label: SimpleLabel,
    selected: Boolean,
    onLabelClick: () -> Unit,
) {
    ListItem(
        leadingContent = {
            Icon(imageVector = Icons.Outlined.Label, contentDescription = null)
        },
        headlineText = { Text(text = label.name) },
        trailingContent = {
            Checkbox(
                checked = selected,
                onCheckedChange = {},
                colors = CheckboxDefaults.colors(
                    checkedColor = finitoColors.tertiary
                )
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLabelClick)
    )
}