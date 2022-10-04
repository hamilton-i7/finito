package com.example.finito.features.boards.presentation.screen.board.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.finitoColors
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardLabels(
    labels: List<SimpleLabel>,
    modifier: Modifier = Modifier,
    onLabelClick: () -> Unit = {},
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        mainAxisSpacing = 8.dp
    ) {
        labels.forEach { label ->
            SuggestionChip(
                onClick = onLabelClick,
                label = { Text(text = label.name) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = finitoColors.surfaceVariant,
                    labelColor = finitoColors.onSurfaceVariant
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    borderColor = finitoColors.surfaceVariant
                )
            )
        }
    }
}