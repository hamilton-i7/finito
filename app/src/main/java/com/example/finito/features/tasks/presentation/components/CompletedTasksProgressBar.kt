package com.example.finito.features.tasks.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.ui.theme.finitoColors

@Composable
fun CompletedTasksProgressBar(
    totalTasks: Int,
    completedTasks: Int,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(targetValue = completedTasks / totalTasks.toFloat())

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.tasks_completed),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = "$completedTasks/$totalTasks")
        }
        LinearProgressIndicator(
            progress = progress,
            trackColor = finitoColors.tertiaryContainer,
            color = finitoColors.onTertiaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(24.dp))
        )
    }
}