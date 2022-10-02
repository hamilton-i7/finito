package com.example.finito.features.subtasks.presentation.screen.editsubtask

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.textfields.FinitoTextField
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.core.presentation.util.calculateDp
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.presentation.components.BoardsListSheetContent
import com.example.finito.features.boards.presentation.components.SelectedBoardIndicator
import com.example.finito.features.subtasks.presentation.screen.editsubtask.components.EditSubtaskDialogs
import com.example.finito.features.subtasks.presentation.screen.editsubtask.components.EditSubtaskTopBar
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.launch

private fun handleBackPressToBoardScreen(
    previousRoute: String,
    appViewModel: AppViewModel,
    onNavigateBack: () -> Unit,
) {
    if (previousRoute == Screen.Board.route) {
        appViewModel.onEvent(AppEvent.RefreshBoard)
    }
    onNavigateBack()
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditSubtaskScreen(
    editSubtaskViewModel: EditSubtaskViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    previousRoute: String = "",
    onNavigateBack: () -> Unit = {},
    onShowSnackbar: (
        message: Int,
        actionLabel: Int?,
        onActionClick: () -> Unit
    ) -> Unit = {_, _, _ -> },
) {
    val scope = rememberCoroutineScope()
    val focusManager: FocusManager = LocalFocusManager.current

    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val bottomSheetCorners by animateDpAsState(
        targetValue = calculateDp(bottomSheetState)
    )

    BackHandler {
        if (bottomSheetState.isVisible) {
            scope.launch { bottomSheetState.hide() }
            return@BackHandler
        }
        handleBackPressToBoardScreen(previousRoute, appViewModel, onNavigateBack)
    }

    LaunchedEffect(Unit) {
        editSubtaskViewModel.eventFlow.collect { event ->
            when (event) {
                is EditSubtaskViewModel.Event.SubtaskUpdated -> {
                    handleBackPressToBoardScreen(previousRoute, appViewModel, onNavigateBack)
                }
                is EditSubtaskViewModel.Event.ShowError -> {
                    editSubtaskViewModel.onEvent(EditSubtaskEvent.ShowDialog(
                        type = EditSubtaskEvent.DialogType.Error(event.error)
                    ))
                }
                is EditSubtaskViewModel.Event.Snackbar -> {
                    when (event) {
                        is EditSubtaskViewModel.Event.Snackbar.RecoverSubtask -> {
                            onShowSnackbar(event.message, R.string.undo) {
                                appViewModel.onEvent(AppEvent.RecoverSubtask(
                                    subtask = event.subtask
                                ))
                            }
                        }
                        is EditSubtaskViewModel.Event.Snackbar.UndoSubtaskChange -> {
                            onShowSnackbar(event.message, R.string.undo) {
                                appViewModel.onEvent(AppEvent.UndoSubtaskCompletedToggle(
                                    subtask = event.subtask
                                ))
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        appViewModel.event.collect { event ->
            if (event != AppViewModel.Event.RefreshSubtask) return@collect
            editSubtaskViewModel.onEvent(EditSubtaskEvent.RefreshSubtask)
        }
    }
    
    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = bottomSheetCorners, topEnd = bottomSheetCorners),
        sheetBackgroundColor = finitoColors.surfaceColorAtElevation(3.dp),
        sheetContent = {
            BoardsListSheetContent(
                boards = editSubtaskViewModel.boards,
                selectedBoard = editSubtaskViewModel.selectedBoard,
                onBoardClick = {
                    editSubtaskViewModel.onEvent(EditSubtaskEvent.ChangeBoard(it))
                    scope.launch { bottomSheetState.hide() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                EditSubtaskTopBar(
                    subtaskCompleted = editSubtaskViewModel.subtask?.completed ?: false,
                    onNavigationIconClick = {
                        handleBackPressToBoardScreen(previousRoute, appViewModel, onNavigateBack)
                    },
                    onToggleTaskCompleted = {
                        editSubtaskViewModel.onEvent(EditSubtaskEvent.ToggleCompleted)
                    },
                    onDeleteTask = {
                        editSubtaskViewModel.onEvent(EditSubtaskEvent.DeleteSubtask)
                    },
                    scrollBehavior = topBarScrollBehavior
                )
            },
            modifier = Modifier
                .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                .noRippleClickable { focusManager.clearFocus() }
        ) { innerPadding ->
            EditSubtaskDialogs(editSubtaskViewModel)

            EditSubtaskScreen(
                paddingValues = innerPadding,
                selectedBoardName = editSubtaskViewModel.selectedBoard?.name ?: "",
                showBoardsMenu = bottomSheetState.isVisible,
                onBoardIndicatorClick = {
                    scope.launch { bottomSheetState.show() }
                },
                nameState = editSubtaskViewModel.nameState.copy(
                    onValueChange = {
                        editSubtaskViewModel.onEvent(EditSubtaskEvent.ChangeName(it))
                    }
                ),
                descriptionState = editSubtaskViewModel.descriptionState.copy(
                    onValueChange = {
                        editSubtaskViewModel.onEvent(EditSubtaskEvent.ChangeDescription(it))
                    }
                ),
                onEditButtonClick = {
                    editSubtaskViewModel.onEvent(EditSubtaskEvent.EditSubtask)
                }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun EditSubtaskScreen(
    paddingValues: PaddingValues = PaddingValues(),
    selectedBoardName: String = "",
    showBoardsMenu: Boolean = false,
    onBoardIndicatorClick: () -> Unit = {},
    nameState: TextFieldState = TextFieldState(),
    descriptionState: TextFieldState = TextFieldState(),
    onEditButtonClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            SelectedBoardIndicator(
                boardName = selectedBoardName,
                expanded = showBoardsMenu,
                onIndicatorClick = onBoardIndicatorClick,
            )
            Spacer(modifier = Modifier.height(16.dp))

            FinitoTextField(
                value = nameState.value,
                onValueChange = nameState.onValueChange,
                label = { Text(text = stringResource(id = R.string.name)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
            )
            Spacer(modifier = Modifier.height(24.dp))

            FinitoTextField(
                value = descriptionState.value,
                onValueChange = descriptionState.onValueChange,
                singleLine = false,
                label = { Text(text = stringResource(id = R.string.description)) },
            )
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = nameState.value.isNotBlank(),
                transitionSpec = { fadeIn() with fadeOut() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { isValidName ->
                Button(
                    onClick = onEditButtonClick,
                    enabled = isValidName,
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            }
        }
    }
}

@CompletePreviews
@Composable
private fun EditSubtaskScreenPreview() {
    FinitoTheme {
        Surface {
            EditSubtaskScreen(selectedBoardName = "Board name")
        }
    }
}