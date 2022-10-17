package com.example.finito.features.tasks.presentation.screen.addedittask.components

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
fun AddEditTaskTopBar(
    createMode: Boolean = true,
    taskCompleted: Boolean = false,
    onNavigationIconClick: () -> Unit = {},
    onToggleTaskCompleted: () -> Unit = {},
    onDeleteTask: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopBar(
        title = if (createMode) R.string.create_task else R.string.edit_task,
        navigationIcon = R.drawable.back,
        navigationIconDescription = R.string.go_back,
        onNavigationIconClick = onNavigationIconClick,
        scrollBehavior = scrollBehavior,
        actions = actions@{
            if (createMode) return@actions
            IconButton(onClick = onToggleTaskCompleted) {
                if (taskCompleted) {
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
                    contentDescription = stringResource(id = R.string.delete_task)
                )
            }
        }
    )
}