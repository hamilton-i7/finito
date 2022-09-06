package com.example.finito.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.core.domain.util.SortingOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortingChips(
    options: List<SortingOption>,
    selectedOption: SortingOption,
    onOptionClick: (SortingOption) -> Unit = {}
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val selected = option.label == selectedOption.label
            FilterChip(
                selected = selected,
                onClick = { onOptionClick(option) },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                label = { Text(stringResource(id = option.label)) }
            )
        }
    }
}