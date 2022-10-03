package com.example.finito.features.boards.presentation.screen.addeditboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.components.textfields.FinitoTextField
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.LazyListKeys
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.core.presentation.util.menu.DeletedEditBoardScreenMenuOption
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.presentation.screen.addeditboard.components.AddEditBoardDialogs
import com.example.finito.features.boards.presentation.screen.addeditboard.components.AddEditBoardTopBar
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.LabelItem
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBoardScreen(
    appViewModel: AppViewModel,
    createMode: Boolean,
    onShowSnackbar: (message: Int, actionLabel: Int?, onActionClick: () -> Unit) -> Unit,
    addEditBoardViewModel: AddEditBoardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToArchive: () -> Unit = {},
    onNavigateToTrash: () -> Unit = {},
    onNavigateToBoardFlow: (boardId: Int) -> Unit = {},
    onNavigateToBoard: (boardId: Int, BoardState) -> Unit = {_, _ -> },
) {
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current

    BackHandler {
        if (createMode) {
            onNavigateBack()
            return@BackHandler
        }
        onNavigateToBoard(
            addEditBoardViewModel.board!!.board.boardId,
            addEditBoardViewModel.boardState
        )
    }

    LaunchedEffect(Unit) {
        addEditBoardViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditBoardViewModel.Event.Snackbar -> {
                    when (event) {
                        AddEditBoardViewModel.Event.Snackbar.RestoredBoard -> {
                            onShowSnackbar(event.message, event.actionLabel) {
                                addEditBoardViewModel.onEvent(AddEditBoardEvent.UndoRestore)
                            }
                        }
                        AddEditBoardViewModel.Event.Snackbar.UneditableBoard -> {
                            onShowSnackbar(event.message, event.actionLabel) {
                                addEditBoardViewModel.onEvent(
                                    AddEditBoardEvent.RestoreBoard()
                                )
                            }
                        }
                        AddEditBoardViewModel.Event.Snackbar.DeletedBoard -> {
                            onShowSnackbar(event.message, event.actionLabel) {
                                appViewModel.onEvent(AppEvent.UndoBoardChange(
                                    board = addEditBoardViewModel.board!!
                                ))
                            }
                        }
                    }
                }
                is AddEditBoardViewModel.Event.NavigateToCreatedBoard -> {
                    onNavigateToBoardFlow(event.id)
                }
                is AddEditBoardViewModel.Event.NavigateToUpdatedBoard -> {
                    onNavigateToBoard(event.id, addEditBoardViewModel.boardState)
                }
                is AddEditBoardViewModel.Event.ShowError -> {
                    addEditBoardViewModel.onEvent(AddEditBoardEvent.ShowDialog(
                        type = AddEditBoardEvent.DialogType.Error(message = event.error)
                    ))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AddEditBoardTopBar(
                boardState = addEditBoardViewModel.boardState,
                createMode = createMode,
                showMenu = addEditBoardViewModel.showScreenMenu,
                scrollBehavior = topBarScrollBehavior,
                onNavigationIconClick = onNavigationIconClick@{
                    if (createMode) {
                        onNavigateBack()
                        return@onNavigationIconClick
                    }
                    onNavigateToBoard(
                        addEditBoardViewModel.board!!.board.boardId,
                        addEditBoardViewModel.boardState
                    )
                },
                onMoveToTrashClick = {
                    addEditBoardViewModel.onEvent(AddEditBoardEvent.MoveBoardToTrash)
                    if (addEditBoardViewModel.boardState == BoardState.ACTIVE) {
                        onNavigateToHome()
                    } else if (addEditBoardViewModel.boardState == BoardState.ARCHIVED) {
                        onNavigateToArchive()
                    }
                },
                onRestoreBoardClick = {
                    addEditBoardViewModel.onEvent(
                        AddEditBoardEvent.RestoreBoard(showSnackbar = true)
                    )
                },
                onMoreOptionsClick = {
                    addEditBoardViewModel.onEvent(
                        AddEditBoardEvent.ShowScreenMenu(show = true)
                    )
                },
                onDismissMenu = {
                    addEditBoardViewModel.onEvent(
                        AddEditBoardEvent.ShowScreenMenu(show = false)
                    )
                },
                onMenuOptionClick = { option ->
                    addEditBoardViewModel.onEvent(
                        AddEditBoardEvent.ShowScreenMenu(show = false)
                    )
                    when (option) {
                        DeletedEditBoardScreenMenuOption.DeleteForever -> {
                            addEditBoardViewModel.onEvent(
                                AddEditBoardEvent.ShowDialog(
                                    type = AddEditBoardEvent
                                        .DialogType
                                        .DeleteForever
                                )
                            )
                        }
                    }
                }
            )
        },
        modifier = Modifier
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .noRippleClickable { focusManager.clearFocus() }
    ) { innerPadding ->
        AddEditBoardDialogs(addEditBoardViewModel, onNavigateToTrash)

        AddEditBoardScreen(
            paddingValues = innerPadding,
            createMode = createMode,
            isDeleted = addEditBoardViewModel.boardState == BoardState.DELETED,
            nameState = addEditBoardViewModel.nameState.copy(
                onValueChange = {
                    addEditBoardViewModel.onEvent(AddEditBoardEvent.ChangeName(it))
                }
            ),
            showLabels = addEditBoardViewModel.showLabels,
            onShowLabelsChange = {
                addEditBoardViewModel.onEvent(AddEditBoardEvent.ToggleLabelsVisibility)
            },
            labels = addEditBoardViewModel.labels,
            selectedLabels = addEditBoardViewModel.selectedLabels,
            onLabelClick = {
                addEditBoardViewModel.onEvent(AddEditBoardEvent.SelectLabel(it))
            },
            onButtonClick = onButtonClick@{
                if (createMode) {
                    addEditBoardViewModel.onEvent(AddEditBoardEvent.CreateBoard)
                    return@onButtonClick
                }
                addEditBoardViewModel.onEvent(AddEditBoardEvent.EditBoard)
            },
            onScreenClick = {
                addEditBoardViewModel.onEvent(AddEditBoardEvent.AlertNotEditable)
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun AddEditBoardScreen(
    paddingValues: PaddingValues = PaddingValues(),
    createMode: Boolean = true,
    isDeleted: Boolean = false,
    nameState: TextFieldState = TextFieldState.Default,
    showLabels: Boolean = true,
    onShowLabelsChange: () -> Unit = {},
    labels: List<SimpleLabel> = emptyList(),
    selectedLabels: List<SimpleLabel> = emptyList(),
    onLabelClick: (SimpleLabel) -> Unit = {},
    onButtonClick: () -> Unit = {},
    onScreenClick: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    val selectedLabelsIds = selectedLabels.groupBy { it.labelId }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .noRippleClickable(onClick = {
                if (isDeleted) onScreenClick()
            })
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(contentType = { ContentTypes.NAME_TEXT_FIELD }) {
                FinitoTextField(
                    value = nameState.value,
                    onValueChange = nameState.onValueChange,
                    enabled = !isDeleted,
                    label = { Text(text = stringResource(id = R.string.name)) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .focusRequester(focusRequester)
                        .animateItemPlacement()
                )
                Spacer(modifier = Modifier.height(32.dp).animateItemPlacement())
            }
            item(contentType = ContentTypes.LABELS_TOGGLE) {
                RowToggle(
                    showContent = showLabels,
                    onShowContentToggle = onShowLabelsChange,
                    label = stringResource(id = R.string.labels),
                    showContentDescription = R.string.show_labels,
                    hideContentDescription = R.string.hide_labels,
                    modifier = Modifier.animateItemPlacement()
                )
            }
            items(labels, key = { it.labelId }, contentType = { ContentTypes.LABELS }) {
                AnimatedVisibility(
                    visible = showLabels,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.animateItemPlacement()
                ) {
                    LabelItem(
                        label = it,
                        selected = selectedLabelsIds[it.labelId] != null,
                        enabled = !isDeleted,
                        onLabelClick = { onLabelClick(it) }
                    )
                }
            }
            item(key = LazyListKeys.PRIMARY_BUTTON) {
                Spacer(modifier = Modifier.height(40.dp).animateItemPlacement())
                AnimatedContent(
                    targetState = nameState.value.isNotBlank(),
                    transitionSpec = { fadeIn() with fadeOut() },
                    modifier = Modifier.animateItemPlacement()
                ) { validName ->
                    Button(
                        onClick = onButtonClick,
                        enabled = validName && !isDeleted,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .widthIn(max = 350.dp)
                            .fillMaxWidth()
                    ) {
                        if (createMode) {
                            Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = stringResource(
                            id = if (createMode) R.string.create_board else R.string.edit_board
                        ))
                    }
                }
            }
        }
    }
}

@CompletePreviews
@Composable
private fun AddEditBoardScreenPreview() {
    val labels = SimpleLabel.dummyLabels.take(6)
    val selectedLabels = labels.take(3)

    FinitoTheme {
        Surface {
            AddEditBoardScreen(
                nameState = TextFieldState.Default,
                labels = SimpleLabel.dummyLabels.take(6),
                selectedLabels = selectedLabels
            )
        }
    }
}