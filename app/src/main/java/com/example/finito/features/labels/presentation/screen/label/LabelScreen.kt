package com.example.finito.features.labels.presentation.screen.label

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.core.presentation.util.menu.ActiveBoardCardMenuOption
import com.example.finito.core.presentation.util.menu.LabelMenuOption
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.labels.presentation.screen.label.components.LabelDialogs
import com.example.finito.features.labels.presentation.screen.label.components.LabelTopBar
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LabelScreen(
    labelViewModel: LabelViewModel = hiltViewModel(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    onShowSnackbar: (
        message: Int,
        actionLabel: Int?,
        onActionClick: () -> Unit
    ) -> Unit = { _, _, _ -> },
    onNavigateToHome: () -> Unit = {},
    onNavigateToCreateBoard: () -> Unit = {},
    onNavigateToBoardFlow: (Int) -> Unit = {},
) {
    val label = labelViewModel.label

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchTopBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            return@BackHandler
        }
        if (labelViewModel.showSearchBar) {
            labelViewModel.onEvent(LabelEvent.ShowSearchBar(show = false))
            return@BackHandler
        }
        onNavigateToHome()
    }

    LaunchedEffect(Unit) {
        labelViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LabelViewModel.Event.ShowSnackbar -> {
                    onShowSnackbar(event.message, R.string.undo) {
                        labelViewModel.onEvent(LabelEvent.RestoreBoard)
                    }
                }
                is LabelViewModel.Event.ShowError -> {
                    labelViewModel.onEvent(LabelEvent.ShowDialog(
                        type = LabelEvent.DialogType.Error(message = event.error)
                    ))
                }
                LabelViewModel.Event.NavigateHome -> onNavigateToHome()
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = labelViewModel.showSearchBar,
            ) { showSearchBar ->
                if (showSearchBar) {
                    SearchTopBar(
                        queryState = labelViewModel.searchQueryState.copy(
                            onValueChange = {
                                labelViewModel.onEvent(LabelEvent.SearchBoards(it))
                            }
                        ),
                        onBackClick = {
                            labelViewModel.onEvent(LabelEvent.ShowSearchBar(show = false))
                        },
                        scrollBehavior = searchTopBarScrollBehavior,
                        focusRequester = focusRequester
                    )
                } else {
                    LabelTopBar(
                        labelName = label?.name ?: "",
                        onNavigationClick = {
                            scope.launch { drawerState.open() }
                        },
                        showMenu = labelViewModel.showScreenMenu,
                        onMoreOptionsClick = {
                            labelViewModel.onEvent(LabelEvent.ShowScreenMenu(show = true))
                        },
                        onDismissMenu = {
                            labelViewModel.onEvent(LabelEvent.ShowScreenMenu(show = false))
                        },
                        onOptionClick = { option ->
                            labelViewModel.onEvent(LabelEvent.ShowScreenMenu(show = false))

                            when (option) {
                                LabelMenuOption.DeleteLabel -> {
                                    labelViewModel.onEvent(LabelEvent.ShowDialog(
                                        type = LabelEvent.DialogType.Delete
                                    ))
                                }
                                LabelMenuOption.RenameLabel -> {
                                    labelViewModel.onEvent(LabelEvent.ShowDialog(
                                        type = LabelEvent.DialogType.Rename
                                    ))
                                }
                            }
                        },
                        scrollBehavior = topBarScrollBehavior
                    )
                }
            }
        },
        bottomBar = {
            BottomBar(
                fabDescription = R.string.add_board,
                searchDescription = R.string.search_boards,
                onChangeLayoutClick = {
                    labelViewModel.onEvent(LabelEvent.ToggleLayout)
                },
                gridLayout = labelViewModel.gridLayout,
                onSearchClick = {
                    labelViewModel.onEvent(LabelEvent.ShowSearchBar(show = true))
                },
                onFabClick = onNavigateToCreateBoard
            )
        },
        modifier = Modifier
            .nestedScroll(
                if (labelViewModel.showSearchBar)
                    searchTopBarScrollBehavior.nestedScrollConnection
                else
                    topBarScrollBehavior.nestedScrollConnection
            )
            .noRippleClickable { focusManager.clearFocus() }
            .testTag(TestTags.LABEL_SCREEN),
    ) { innerPadding ->
        LabelDialogs(labelViewModel)

        LabelScreen(
            paddingValues = innerPadding,
            gridLayout = labelViewModel.gridLayout,
            boards = labelViewModel.boards,
            onBoardClick = onNavigateToBoardFlow,
            selectedSortingOption = labelViewModel.boardsOrder,
            onSortOptionClick = {
                labelViewModel.onEvent(LabelEvent.SortBoards(it))
            },
            onCardOptionsClick = {
                labelViewModel.onEvent(LabelEvent.ShowCardMenu(boardId = it, show = true))
            },
            showCardMenu = { labelViewModel.selectedBoardId == it },
            onDismissMenu = {
                labelViewModel.onEvent(LabelEvent.ShowCardMenu(show = false))
            },
            options = listOf(
                ActiveBoardCardMenuOption.Archive,
                ActiveBoardCardMenuOption.Delete,
            ),
            onMenuItemClick = { board, option ->
                labelViewModel.onEvent(LabelEvent.ShowCardMenu(show = false))
                when (option) {
                    ActiveBoardCardMenuOption.Archive -> {
                        labelViewModel.onEvent(LabelEvent.ArchiveBoard(board))
                    }
                    ActiveBoardCardMenuOption.Delete -> {
                        labelViewModel.onEvent(LabelEvent.MoveBoardToTrash(board))
                    }
                }
            }
        )
    }
}

@Composable
private fun LabelScreen(
    paddingValues: PaddingValues = PaddingValues(),
    gridLayout: Boolean = true,
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    selectedSortingOption: SortingOption.Common = SortingOption.Common.NameAZ,
    onSortOptionClick: (option: SortingOption.Common) -> Unit = {},
    onBoardClick: (boardId: Int) -> Unit = {},
    showCardMenu: (boardId: Int) -> Boolean = { false },
    onDismissMenu: (boardId: Int) -> Unit = {},
    options: List<ActiveBoardCardMenuOption> = emptyList(),
    onCardOptionsClick: (boardId: Int) -> Unit = {},
    onMenuItemClick: (
        board: BoardWithLabelsAndTasks,
        option: ActiveBoardCardMenuOption,
    ) -> Unit = { _, _ ->}
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        BoardLayout(
            gridLayout = gridLayout,
            boards = boards,
            sortingOptions = SortingOption.Common.options,
            selectedSortingOption = selectedSortingOption,
            onSortOptionClick = onSortOptionClick,
            onBoardClick = onBoardClick,
            showCardMenu = showCardMenu,
            onDismissMenu = onDismissMenu,
            options = options,
            onCardOptionsClick = onCardOptionsClick,
            onMenuItemClick = { boardId, option ->
                onMenuItemClick(boardId, option as ActiveBoardCardMenuOption)
            },
        )
    }
}

@ThemePreviews
@Composable
private fun LabelScreenPreview() {
    FinitoTheme {
        Surface {
            LabelScreen(
                boards = BoardWithLabelsAndTasks.dummyBoards,
                selectedSortingOption = SortingOption.Common.Newest
            )
        }
    }
}