package com.example.finito.features.boards.presentation.screen.addeditboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.components.bars.TopBar
import com.example.finito.core.presentation.components.menu.FinitoMenu
import com.example.finito.core.presentation.util.menu.DeletedEditBoardScreenMenuOption
import com.example.finito.features.boards.domain.entity.BoardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBoardTopBar(
    boardState: BoardState,
    createMode: Boolean = true,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavigationIconClick: () -> Unit = {},
    onMoveToTrashClick: () -> Unit = {},
    onRestoreBoardClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    onMenuOptionClick: (DeletedEditBoardScreenMenuOption) -> Unit = {},
) {
    TopBar(
        title = if (createMode) R.string.create_board else R.string.edit_board,
        navigationIcon = Icons.Outlined.ArrowBack,
        navigationIconDescription = R.string.go_back,
        scrollBehavior = scrollBehavior,
        onNavigationIconClick = onNavigationIconClick,
        actions = actions@{
            if (createMode) return@actions

            when (boardState) {
                BoardState.ACTIVE, BoardState.ARCHIVED -> {
                    IconButton(onClick = onMoveToTrashClick) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(id = R.string.move_to_trash)
                        )
                    }
                }
                BoardState.DELETED -> {
                    IconButton(onClick = onRestoreBoardClick) {
                        Icon(
                            imageVector = Icons.Outlined.Restore,
                            contentDescription = stringResource(id = R.string.restore_board)
                        )
                    }
                    Box {
                        IconButton(onClick = onMoreOptionsClick) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(id = R.string.more_options)
                            )
                        }
                        FinitoMenu(
                            show = showMenu,
                            onDismiss = onDismissMenu,
                            options = listOf<DeletedEditBoardScreenMenuOption>(
                                DeletedEditBoardScreenMenuOption.DeleteForever
                            ),
                            onOptionClick = { option -> onMenuOptionClick(option) }
                        )
                    }
                }
            }
        }
    )
}