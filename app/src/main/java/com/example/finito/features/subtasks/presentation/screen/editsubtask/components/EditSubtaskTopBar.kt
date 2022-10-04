package com.example.finito.features.subtasks.presentation.screen.editsubtask.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.RemoveDone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
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
        navigationIcon = Icons.Outlined.ArrowBack,
        navigationIconDescription = R.string.go_back,
        onNavigationIconClick = onNavigationIconClick,
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(onClick = onToggleTaskCompleted) {
                if (subtaskCompleted) {
                    Icon(
                        imageVector = Icons.Outlined.RemoveDone,
                        contentDescription = stringResource(id = R.string.mark_as_uncompleted)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.DoneAll,
                        contentDescription = stringResource(id = R.string.mark_as_completed)
                    )
                }
            }
            IconButton(onClick = onDeleteTask) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(id = R.string.delete_subtask)
                )
            }
        }
    )
}