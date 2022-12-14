package com.example.finito.features.tasks.presentation.screen.addedittask

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.textfields.DateTextField
import com.example.finito.core.presentation.components.textfields.FinitoTextField
import com.example.finito.core.presentation.components.textfields.PriorityChips
import com.example.finito.core.presentation.components.textfields.TimeTextField
import com.example.finito.core.presentation.util.*
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.presentation.components.BoardsListSheetContent
import com.example.finito.features.boards.presentation.components.SelectedBoardIndicator
import com.example.finito.features.subtasks.presentation.components.SubtaskTextFieldItem
import com.example.finito.features.tasks.domain.util.Priority
import com.example.finito.features.tasks.presentation.screen.addedittask.components.AddEditTaskDialogs
import com.example.finito.features.tasks.presentation.screen.addedittask.components.AddEditTaskTopBar
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDate
import java.time.LocalTime

private fun handleBackNavigation(
    previousRoute: String,
    onNavigateToBoard: (boardId: Int, BoardState) -> Unit,
    onNavigateBack: () -> Unit,
    addEditTaskViewModel: AddEditTaskViewModel
) {
    if (previousRoute == Screen.Board.route) {
        onNavigateToBoard(
            addEditTaskViewModel.originalRelatedBoard!!.boardId,
            addEditTaskViewModel.originalRelatedBoard!!.state
        )
    } else { onNavigateBack() }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class)
@Composable
fun AddEditTaskScreen(
    createMode: Boolean,
    previousRoute: String = "",
    addEditTaskViewModel: AddEditTaskViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onNavigateToBoard: (boardId: Int, BoardState) -> Unit = {_, _ -> },
    onNavigateBack: () -> Unit = {},
    onShowSnackbar: (
        message: Int,
        actionLabel: Int?,
        onActionClick: () -> Unit
    ) -> Unit = {_, _, _ -> },
) {
    val window = (LocalView.current.context as Activity).window
    val scope = rememberCoroutineScope()
    val focusManager: FocusManager = LocalFocusManager.current
    var focusDirectionToMove by remember { mutableStateOf<FocusDirection?>(null) }
    val postNotificationsPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            permission = android.Manifest.permission.POST_NOTIFICATIONS
        )
    } else null

    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    val hapticFeedback = LocalHapticFeedback.current
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            addEditTaskViewModel.onEvent(AddEditTaskEvent.ReorderSubtasks(from, to))
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        canDragOver = addEditTaskViewModel::canDragTask,
    )
    val bottomSheetCorners by animateDpAsState(
        targetValue = calculateDp(bottomSheetState)
    )

    BackHandler {
        if (bottomSheetState.isVisible) {
            scope.launch { bottomSheetState.hide() }
            return@BackHandler
        }
        handleBackNavigation(previousRoute, onNavigateToBoard, onNavigateBack, addEditTaskViewModel)
    }

    LaunchedEffect(addEditTaskViewModel.subtaskNameStates) {
        focusDirectionToMove?.let(focusManager::moveFocus)
        focusDirectionToMove = null
    }

    LaunchedEffect(Unit) {
        addEditTaskViewModel.eventFlow.collect { event ->
            when (event) {
                is AddEditTaskViewModel.Event.NavigateBack -> {
                    handleBackNavigation(previousRoute, onNavigateToBoard, onNavigateBack, addEditTaskViewModel)
                }
                is AddEditTaskViewModel.Event.ShowError -> {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog(
                        type = AddEditTaskEvent.DialogType.Error(event.error)
                    ))
                }
                is AddEditTaskViewModel.Event.Snackbar -> {
                    when (event) {
                        is AddEditTaskViewModel.Event.Snackbar.TaskDeleted -> {
                            onShowSnackbar(event.message, event.actionLabel) {
                                appViewModel.onEvent(AppEvent.RecoverTask(task = event.task))
                            }
                        }
                        is AddEditTaskViewModel.Event.Snackbar.TaskStateChanged -> {
                            onShowSnackbar(event.message, event.actionLabel) {
                                appViewModel.onEvent(AppEvent.UndoTaskCompletedToggle(task = event.task))
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(
        key1 = postNotificationsPermissionState?.status,
        key2 = addEditTaskViewModel.shouldCheckPostNotificationsPermission
    ) {
        if (postNotificationsPermissionState == null) return@LaunchedEffect
        if (!addEditTaskViewModel.shouldCheckPostNotificationsPermission) return@LaunchedEffect

        when (postNotificationsPermissionState.status) {
            PermissionStatus.Granted -> {
                addEditTaskViewModel.onEvent(AddEditTaskEvent.AllowReminder)
            }
            is PermissionStatus.Denied -> {
                if (!addEditTaskViewModel.firstTimeAskingNotificationsPermission) return@LaunchedEffect
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                if (postNotificationsPermissionState.status.shouldShowRationale) {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog(
                        type = AddEditTaskEvent.DialogType.NotificationsPermission
                    ))
                } else {
                    postNotificationsPermissionState.launchPermissionRequest()
                }
                addEditTaskViewModel.onEvent(AddEditTaskEvent.SkipNotificationsPermissionCheck)
            }
        }
    }

    LaunchedEffect(Unit) {
        appViewModel.event.collect { event ->
            if (event != AppViewModel.Event.RefreshTask) return@collect
            addEditTaskViewModel.onEvent(AddEditTaskEvent.RefreshTask)
        }
    }

    LaunchedEffect(Unit) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = bottomSheetCorners, topEnd = bottomSheetCorners),
        sheetBackgroundColor = finitoColors.surfaceColorAtElevation(1.dp),
        sheetContent = {
            BoardsListSheetContent(
                boards = addEditTaskViewModel.boards,
                selectedBoard = addEditTaskViewModel.selectedBoard,
                onBoardClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeBoard(it))
                    scope.launch { bottomSheetState.hide() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AddEditTaskTopBar(
                    createMode = createMode,
                    taskCompleted = addEditTaskViewModel.task?.task?.completed ?: false,
                    onNavigationIconClick = {
                        handleBackNavigation(previousRoute, onNavigateToBoard, onNavigateBack, addEditTaskViewModel)
                    },
                    onToggleTaskCompleted = {
                        addEditTaskViewModel.onEvent(AddEditTaskEvent.ToggleCompleted)
                    },
                    onDeleteTask = {
                        addEditTaskViewModel.onEvent(AddEditTaskEvent.DeleteTask)
                    },
                    scrollBehavior = topBarScrollBehavior
                )
            },
            modifier = Modifier
                .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                .noRippleClickable { focusManager.clearFocus() }
        ) { innerPadding ->
            AddEditTaskDialogs(addEditTaskViewModel, postNotificationsPermissionState)

            AddEditTaskScreen(
                paddingValues = innerPadding,
                createMode = createMode,
                hapticFeedback = hapticFeedback,
                focusManager = focusManager,
                reorderableState = reorderableState,
                selectedBoardName = addEditTaskViewModel.selectedBoard?.name ?: "",
                showBoardsMenu = bottomSheetState.isVisible,
                onBoardIndicatorClick = {
                    scope.launch { bottomSheetState.show() }
                },
                nameState = addEditTaskViewModel.nameState.copy(
                    onValueChange = {
                        addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeName(it))
                    }
                ),
                descriptionState = addEditTaskViewModel.descriptionState.copy(
                    onValueChange = {
                        addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeDescription(it))
                    }
                ),
                date = addEditTaskViewModel.selectedDate,
                onDateClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog(
                        type = AddEditTaskEvent.DialogType.Date
                    ))
                },
                onDateRemove = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeDate(date = null))
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeTime(time = null))
                },
                time = addEditTaskViewModel.selectedTime,
                onTimeClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog(
                        type = AddEditTaskEvent.DialogType.Time
                    ))
                },
                onTimeRemove = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeTime(time = null))
                },
                priority = addEditTaskViewModel.selectedPriority,
                onPriorityClick = onPriorityClick@{
                    if (addEditTaskViewModel.selectedPriority == it) {
                        addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangePriority(priority = null))
                        return@onPriorityClick
                    }
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangePriority(it))
                },
                subtaskTextFields = addEditTaskViewModel.subtaskNameStates.map { state ->
                    state.copy(
                        onValueChange = {
                            addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeSubtaskName(
                                id = state.id,
                                name = it
                            ))
                        }
                    )
                },
                onCreateSubtask = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.CreateSubtask)
                },
                onRemoveSubtask = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.RemoveSubtask(it))
                },
                onRemoveSubtaskByKeyPress = onRemoveSubtaskByKeyPress@{ position, state ->
                    // If it is the first item, just remove the field
                    // Otherwise, remove the field and move focus to previous field
                    if (position == 0) {
                        addEditTaskViewModel.onEvent(AddEditTaskEvent.RemoveSubtask(state))
                        return@onRemoveSubtaskByKeyPress
                    }
                    focusManager.moveFocus(FocusDirection.Up)
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.RemoveSubtask(state))
                },
                onNextSubtask = onNextSubtask@{ position ->
                    // If it is the last item, add a new subtask field
                    // Otherwise, simply move focus
                    if (position != addEditTaskViewModel.subtaskNameStates.lastIndex) {
                        focusManager.moveFocus(FocusDirection.Down)
                        return@onNextSubtask
                    }
                    focusDirectionToMove = FocusDirection.Down
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.CreateSubtask)
                },
                onAddEditButtonClick = onAddEditButtonClick@{
                    if (createMode) {
                        addEditTaskViewModel.onEvent(AddEditTaskEvent.CreateTask)
                        return@onAddEditButtonClick
                    }
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.EditTask)
                }
            )
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class
)
@Composable
private fun AddEditTaskScreen(
    paddingValues: PaddingValues = PaddingValues(),
    createMode: Boolean = true,
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
    focusManager: FocusManager = LocalFocusManager.current,
    reorderableState: ReorderableLazyListState = rememberReorderableLazyListState(
        onMove = { _, _ -> }
    ),
    selectedBoardName: String = "",
    showBoardsMenu: Boolean = false,
    onBoardIndicatorClick: () -> Unit = {},
    nameState: TextFieldState = TextFieldState.Default,
    descriptionState: TextFieldState = TextFieldState.Default,
    date: LocalDate? = null,
    onDateClick: () -> Unit = {},
    onDateRemove: () -> Unit = {},
    time: LocalTime? = null,
    onTimeClick: () -> Unit = {},
    onTimeRemove: () -> Unit = {},
    priority: Priority? = null,
    onPriorityClick: (Priority) -> Unit = {},
    subtaskTextFields: List<SubtaskTextField> = emptyList(),
    onCreateSubtask: () -> Unit = {},
    onRemoveSubtask: (SubtaskTextField) -> Unit = {},
    onRemoveSubtaskByKeyPress: (position: Int, SubtaskTextField) -> Unit = {_, _ -> },
    onNextSubtask: (position: Int) -> Unit = {},
    onAddEditButtonClick: () -> Unit = {},
) {
    val keyboardState by keyboardAsState()

    Surface(modifier = Modifier.padding(paddingValues)) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 12.dp),
            state = reorderableState.listState,
            modifier = Modifier.reorderable(reorderableState)
        ) {
            item(
                key = LazyListKeys.SELECTED_BOARD_INDICATOR,
                contentType = ContentTypes.BOARD_INDICATOR
            ) {
                SelectedBoardIndicator(
                    boardName = selectedBoardName,
                    expanded = showBoardsMenu,
                    onIndicatorClick = onBoardIndicatorClick,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .animateItemPlacement()
                )
            }
            item(
                key = LazyListKeys.NAME_TEXT_FIELD,
                contentType = ContentTypes.TEXT_FIELDS
            ) {
                FinitoTextField(
                    value = nameState.value,
                    onValueChange = nameState.onValueChange,
                    label = { Text(text = stringResource(id = R.string.name)) },
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .animateItemPlacement(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                )
            }
            item(
                key = LazyListKeys.DESCRIPTION_TEXT_FIELD,
                contentType = ContentTypes.TEXT_FIELDS
            ) {
                FinitoTextField(
                    value = descriptionState.value,
                    onValueChange = descriptionState.onValueChange,
                    singleLine = false,
                    label = { Text(text = stringResource(id = R.string.description)) },
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .animateItemPlacement(),
                )
            }
            item(
                key = LazyListKeys.TASK_DATE_TEXT_FIELD,
                contentType = ContentTypes.TEXT_FIELDS
            ) {
                DateTextField(
                    date = date,
                    onClick = onDateClick,
                    onDateRemove = onDateRemove,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .animateItemPlacement()
                )
            }
            item(
                key = LazyListKeys.TASK_TIME_TEXT_FIELD,
                contentType = ContentTypes.TEXT_FIELDS
            ) {
                TimeTextField(
                    time = time,
                    onClick = onTimeClick,
                    onTimeRemove = onTimeRemove,
                    enabled = date != null,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .animateItemPlacement()
                )
            }

            item(
                key = LazyListKeys.TASK_PRIORITY,
                contentType = ContentTypes.PRIORITY_CHIPS
            ) {
                PriorityChips(
                    selectedPriority = priority,
                    onPriorityClick = onPriorityClick,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .animateItemPlacement()
                )
            }

            item(
                key = LazyListKeys.SUBTASKS_TITLE,
                contentType = ContentTypes.SUBTASK_TITLE
            ) {
                Text(
                    text = stringResource(id = R.string.subtasks),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItemPlacement()
                )
            }

            itemsIndexed(
                items = subtaskTextFields,
                key = { _, state -> state.id },
                contentType = { _, _ -> ContentTypes.SUBTASK_TEXT_FIELDS }
            ) { index, textFieldState ->
                val focusRequest = remember { FocusRequester() }
                val defaultTextStyle = MaterialTheme.typography.bodyLarge
                val completedTextStyle = defaultTextStyle.copy(
                    textDecoration = TextDecoration.LineThrough
                )
                var textStyle by remember { mutableStateOf(defaultTextStyle) }

                ReorderableItem(reorderableState, key = textFieldState.id) { isDragging ->
                    SubtaskTextFieldItem(
                        state = textFieldState,
                        reorderableState = reorderableState,
                        hapticFeedback = hapticFeedback,
                        focusManager = focusManager,
                        textStyle = textStyle,
                        isDragging = isDragging,
                        showDragIndicator = keyboardState == Keyboard.CLOSED,
                        readOnly = reorderableState.draggingItemKey != null,
                        onRemoveSubtask = { onRemoveSubtask(textFieldState) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { onNextSubtask(index) }
                        ),
                        textFieldModifier = Modifier
                            .focusRequester(focusRequest)
                            .onFocusChanged {
                                textStyle =
                                    if (it.isFocused || !textFieldState.completed) defaultTextStyle
                                    else completedTextStyle
                            }
                            .onKeyEvent { event ->
                                if (event.key != Key.Backspace) return@onKeyEvent false
                                if (textFieldState.value.isNotEmpty()) return@onKeyEvent false
                                onRemoveSubtaskByKeyPress(index, textFieldState)
                                true
                            }
                    )
                }
            }

            item(
                key = LazyListKeys.CREATE_SUBTASK_BUTTON,
                contentType = ContentTypes.SECONDARY_BUTTON
            ) {
                TextButton(
                    onClick = onCreateSubtask,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItemPlacement()
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.create_subtask))
                }
            }

            item(key = LazyListKeys.PRIMARY_BUTTON) {
                Spacer(
                    modifier = Modifier
                        .height(40.dp)
                        .animateItemPlacement()
                )
                AnimatedContent(
                    targetState = nameState.value.isNotBlank(),
                    transitionSpec = { fadeIn() with fadeOut() },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItemPlacement()
                ) { isValidName ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onAddEditButtonClick,
                            enabled = isValidName,
                            modifier = Modifier
                                .widthIn(max = 350.dp)
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        ) {
                            if (createMode) {
                                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(text = stringResource(
                                id = if (createMode) R.string.create_task else R.string.confirm
                            ))
                        }
                    }
                }
            }
        }
    }
}

@CompletePreviews
@Composable
private fun AddEditTaskScreenPreview() {
    FinitoTheme {
        Surface {
            AddEditTaskScreen(
                selectedBoardName = "Board name",
                subtaskTextFields = listOf(
                    SubtaskTextField(id = 1, value = "Subtask 1"),
                    SubtaskTextField(id = 2, value = "Subtask 2"),
                    SubtaskTextField(id = 3),
                )
            )
        }
    }
}