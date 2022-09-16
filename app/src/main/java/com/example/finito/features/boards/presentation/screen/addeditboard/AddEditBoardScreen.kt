package com.example.finito.features.boards.presentation.screen.addeditboard

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.finito.R
import com.example.finito.core.presentation.util.menu.DeletedEditBoardScreenMenuOption
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.components.bars.TopBar
import com.example.finito.core.presentation.components.menu.FinitoMenu
import com.example.finito.core.presentation.components.textfields.FinitoTextField
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.presentation.SharedBoardEvent
import com.example.finito.features.boards.presentation.SharedBoardViewModel
import com.example.finito.features.boards.presentation.screen.addeditboard.components.AddEditBoardDialogs
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.LabelItem
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBoardScreen(
    navController: NavController,
    sharedBoardViewModel: SharedBoardViewModel,
    createMode: Boolean,
    showSnackbar: (message: Int, actionLabel: Int?, onActionClick: (() -> Unit)?) -> Unit,
    addEditBoardViewModel: AddEditBoardViewModel = hiltViewModel(),
) {
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        addEditBoardViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditBoardViewModel.Event.Navigate -> {
                    navController.navigate(route = event.route) {
                        event.popUpRoute?.let { popUpTo(it) { inclusive = true } }
                    }
                }
                is AddEditBoardViewModel.Event.Snackbar -> {
                    when (event) {
                        AddEditBoardViewModel.Event.Snackbar.RestoredBoard -> {
                            showSnackbar(event.message, event.actionLabel) {
                                addEditBoardViewModel.onEvent(AddEditBoardEvent.UndoRestore)
                            }
                        }
                        AddEditBoardViewModel.Event.Snackbar.UneditableBoard -> {
                            showSnackbar(event.message, event.actionLabel) {
                                addEditBoardViewModel.onEvent(
                                    AddEditBoardEvent.RestoreBoard()
                                )
                            }
                        }
                        AddEditBoardViewModel.Event.Snackbar.DeletedBoard -> {
                            showSnackbar(event.message, event.actionLabel) {
                                sharedBoardViewModel.onEvent(SharedBoardEvent.UndoBoardChange(
                                    board = addEditBoardViewModel.board!!
                                ))
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = if (createMode) R.string.create_board else R.string.edit_board,
                navigationIcon = Icons.Outlined.ArrowBack,
                navigationIconDescription = R.string.go_back,
                scrollBehavior = topBarScrollBehavior,
                onNavigationIconClick = { navController.navigateUp() },
                actions = actions@{
                    if (createMode) return@actions
                    when (addEditBoardViewModel.boardState) {
                        BoardState.ACTIVE, BoardState.ARCHIVED -> {
                            IconButton(onClick = {
                                addEditBoardViewModel.onEvent(AddEditBoardEvent.MoveBoardToTrash)
                                if (addEditBoardViewModel.boardState == BoardState.ACTIVE) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                } else if (addEditBoardViewModel.boardState == BoardState.ARCHIVED) {
                                    navController.navigate(Screen.Archive.route) {
                                        popUpTo(Screen.Archive.route) { inclusive = true }
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(id = R.string.move_to_trash)
                                )
                            }
                        }
                        BoardState.DELETED -> {
                            IconButton(onClick = {
                                addEditBoardViewModel.onEvent(
                                    AddEditBoardEvent.RestoreBoard(showSnackbar = true)
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Restore,
                                    contentDescription = stringResource(id = R.string.restore_board)
                                )
                            }
                            Box {
                                IconButton(onClick = {
                                    addEditBoardViewModel.onEvent(
                                        AddEditBoardEvent.ShowScreenMenu(show = true)
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.MoreVert,
                                        contentDescription = stringResource(id = R.string.more_options)
                                    )
                                }
                                FinitoMenu(
                                    show = addEditBoardViewModel.showScreenMenu,
                                    onDismiss = {
                                        addEditBoardViewModel.onEvent(
                                            AddEditBoardEvent.ShowScreenMenu(show = false)
                                        )
                                    },
                                    options = listOf<DeletedEditBoardScreenMenuOption>(
                                        DeletedEditBoardScreenMenuOption.DeleteForever
                                    ),
                                    onOptionClick = { option ->
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
                            }
                        }
                    }
                }
            )
        },
        modifier = Modifier
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .noRippleClickable { focusManager.clearFocus() }
    ) { innerPadding ->
        if (addEditBoardViewModel.dialogType != null) {
            AddEditBoardDialogs(addEditBoardViewModel, navController)
        }

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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AddEditBoardScreen(
    paddingValues: PaddingValues = PaddingValues(),
    createMode: Boolean = true,
    isDeleted: Boolean = false,
    nameState: TextFieldState = TextFieldState(),
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(contentType = { "name text field" }) {
                FinitoTextField(
                    value = nameState.value,
                    onValueChange = nameState.onValueChange,
                    enabled = !isDeleted,
                    label = { Text(text = stringResource(id = R.string.name)) },
                    errorFeedback = R.string.empty_name_error,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item(contentType = "labels toggle") {
                RowToggle(
                    showContent = showLabels,
                    onShowContentToggle = onShowLabelsChange,
                    label = stringResource(id = R.string.labels),
                    showContentDescription = R.string.show_labels,
                    hideContentDescription = R.string.hide_labels
                )
            }
            items(labels, key = { it.labelId }, contentType = { "labels" }) {
                AnimatedVisibility(
                    visible = showLabels,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LabelItem(
                        label = it,
                        selected = selectedLabelsIds[it.labelId] != null,
                        enabled = !isDeleted,
                        onLabelClick = { onLabelClick(it) }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
                AnimatedContent(
                    targetState = nameState.value.isNotBlank(),
                    transitionSpec = { fadeIn() with fadeOut() }
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

@Preview(showBackground = true)
@Composable
private fun AddEditBoardScreenPreview() {
    val labels = SimpleLabel.dummyLabels.take(6)
    val selectedLabels = labels.take(3)

    FinitoTheme {
        Surface {
            AddEditBoardScreen(
                nameState = TextFieldState(),
                labels = SimpleLabel.dummyLabels.take(6),
                selectedLabels = selectedLabels
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AddEditBoardScreenPreviewDark() {
    val labels = SimpleLabel.dummyLabels.take(6)
    val selectedLabels = labels.take(3)

    FinitoTheme {
        Surface {
            AddEditBoardScreen(
                nameState = TextFieldState(value = "Board name"),
                labels = SimpleLabel.dummyLabels.take(6),
                selectedLabels = selectedLabels
            )
        }
    }
}