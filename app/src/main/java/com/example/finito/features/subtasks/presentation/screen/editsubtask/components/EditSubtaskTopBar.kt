package com.example.finito.features.subtasks.presentation.screen.editsubtask.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.components.bars.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubtaskTopBar(
    subtaskCompleted: Boolean = false,
    onNavigationIconClick: () -> Unit = {},
    onToggleTaskCompleted: () -> Unit = {},
    onDeleteTask: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopBar(
        title = R.string.edit_subtask,
        navigationIcon = R.drawable.back,
        navigationIconDescription = R.string.go_back,
        onNavigationIconClick = onNavigationIconClick,
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(onClick = onToggleTaskCompleted) {
                if (subtaskCompleted) {
                    Icon(
                        painter = painterResource(id = R.drawable.remove_double_tick),
                        contentDescription = stringResource(id = R.string.mark_as_uncompleted)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.double_tick),
                        contentDescription = stringResource(id = R.string.mark_as_completed)
                    )
                }
            }
            IconButton(onClick = onDeleteTask) {
                Icon(
                    painter = painterResource(id = R.drawable.trash),
                    contentDescription = stringResource(id = R.string.delete_subtask)
                )
            }
        }
    )
}