package com.example.finito.core.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T: SortingOption> SortingChips(
    options: List<T>,
    selectedOption: T?,
    modifier: Modifier = Modifier,
    onOptionClick: (T) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(id = R.string.sort_by),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val selected = option.label == selectedOption?.label
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
                    label = { Text(stringResource(id = option.label)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = finitoColors.primaryContainer,
                        selectedLabelColor = finitoColors.onPrimaryContainer,
                        selectedLeadingIconColor = finitoColors.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 100)
                        ).testTag(TestTags.SORTING_CHIP)
                )
            }
        }
    }
}