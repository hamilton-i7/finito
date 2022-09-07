package com.example.finito.features.boards.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.components.PercentageIndicator
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.ui.theme.FinitoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardCard(
    onClick: () -> Unit,
    board: BoardWithLabelsAndTasks,
    onOptionsClick: () -> Unit = {}
) {
    val completedTasks = board.tasks.let {
        if (it.isEmpty()) return@let 0F

        val completedAmount = it.filter { task -> task.completed }.size.toFloat()
        completedAmount / it.size
    }
    val labelNames = board.labels.joinToString { it.name }

    Card(onClick) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                PercentageIndicator(
                    progress = completedTasks,
                    modifier = Modifier.padding(start = 16.dp)
                )
                IconButton(onClick = onOptionsClick) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(id = R.string.more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(id = R.string.tasks_amount, board.tasks.size),
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = board.board.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = labelNames.ifEmpty { " " },
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun BoardCardPreview() {
    FinitoTheme {
        BoardCard(
            onClick = {},
            board = BoardWithLabelsAndTasks.dummyBoards.random()
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BoardCardPreviewDark() {
    FinitoTheme {
        BoardCard(
            onClick = {},
            board = BoardWithLabelsAndTasks.dummyBoards.random()
        )
    }
}