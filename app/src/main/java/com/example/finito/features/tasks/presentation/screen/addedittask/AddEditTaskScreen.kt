package com.example.finito.features.tasks.presentation.screen.addedittask

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.domain.Priority
import com.example.finito.core.domain.Reminder
import com.example.finito.core.presentation.components.textfields.DateTextField
import com.example.finito.core.presentation.components.textfields.FinitoTextField
import com.example.finito.core.presentation.components.textfields.PriorityChips
import com.example.finito.core.presentation.components.textfields.TimeTextField
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.LazyListKeys
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.core.presentation.util.menu.TaskReminderOption
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.presentation.components.SelectedBoardIndicator
import com.example.finito.features.subtasks.presentation.components.SubtaskTextFieldItem
import com.example.finito.features.tasks.presentation.screen.addedittask.components.AddEditTaskDialogs
import com.example.finito.features.tasks.presentation.screen.addedittask.components.AddEditTaskTopBar
import com.example.finito.features.tasks.presentation.screen.addedittask.components.ReminderDropdownTextField
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterialApi::class)
private fun calculateDp(bottomSheetState: ModalBottomSheetState): Dp {
    if (bottomSheetState.currentValue == ModalBottomSheetValue.Hidden) return 28.dp
    if (bottomSheetState.currentValue == ModalBottomSheetValue.HalfExpanded) {
        return when (bottomSheetState.direction) {
            -1f -> 28.dp.times(other = 1 - bottomSheetState.progress.fraction)
            else -> 28.dp
        }
    }
    return when (bottomSheetState.direction) {
        -1f, 0f -> 0.dp
        else -> {
            if (bottomSheetState.progress.to == ModalBottomSheetValue.Expanded) 0.dp
            else 28.dp.times(bottomSheetState.progress.fraction)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    createMode: Boolean,
    addEditTaskViewModel: AddEditTaskViewModel = hiltViewModel(),
    onNavigateToBoard: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onShowSnackbar: (
        message: Int,
        actionLabel: Int?,
        onActionClick: () -> Unit
    ) -> Unit = {_, _, _ -> },
) {
    val scope = rememberCoroutineScope()
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
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

    val focusManager: FocusManager = LocalFocusManager.current
    var focusDirectionToMove by remember { mutableStateOf<FocusDirection?>(null) }

    LaunchedEffect(addEditTaskViewModel.subtaskNameStates) {
        focusDirectionToMove?.let(focusManager::moveFocus)
        focusDirectionToMove = null
    }

    LaunchedEffect(Unit) {
        addEditTaskViewModel.eventFlow.collect { event ->
            when (event) {
                is AddEditTaskViewModel.Event.NavigateToBoard -> onNavigateToBoard(event.id)
                is AddEditTaskViewModel.Event.ShowError -> TODO()
                is AddEditTaskViewModel.Event.ShowSnackbar -> TODO()
            }
        }
    }

    BackHandler {
        if (bottomSheetState.isVisible) {
            scope.launch { bottomSheetState.hide() }
            return@BackHandler
        }
        onNavigateBack()
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = bottomSheetCorners, topEnd = bottomSheetCorners),
        sheetBackgroundColor = finitoColors.surface,
        sheetContent = {
            LazyColumn(
                modifier = Modifier.navigationBarsPadding()
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 22.dp)
                                .width(32.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(finitoColors.onSurfaceVariant.copy(alpha = 0.4f))
                        )
                    }
                }
                item {
                    Text(
                        text = stringResource(id = R.string.move_to),
                        style = MaterialTheme.typography.bodySmall,
                        color = finitoColors.outline,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(addEditTaskViewModel.boards) { board ->
                    val selected = board.boardId == addEditTaskViewModel.selectedBoard?.boardId
                    ListItem(
                        headlineText = {
                            Text(
                                text = board.name,
                                color = if (selected) finitoColors.primary else finitoColors.onSurface
                            )
                        },
                        trailingContent = trailingContent@{
                            if (!selected) return@trailingContent
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = stringResource(id = R.string.selected),
                                tint = finitoColors.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeBoard(board))
                                scope.launch { bottomSheetState.hide() }
                            }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                AddEditTaskTopBar(
                    createMode = createMode,
                    taskCompleted = addEditTaskViewModel.task?.task?.completed ?: false,
                    onNavigationIconClick = onNavigateBack,
                    onToggleTaskCompleted = {
                        addEditTaskViewModel.onEvent(AddEditTaskEvent.ToggleCompleted)
                    },
                    onDeleteTask = {
                        addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog(
                            type = AddEditTaskEvent.DialogType.DeleteTask
                        ))
                    },
                    scrollBehavior = topBarScrollBehavior
                )
            },
            modifier = Modifier
                .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                .noRippleClickable { focusManager.clearFocus() }
        ) { innerPadding ->
            AddEditTaskDialogs(addEditTaskViewModel)

            AddEditTaskScreen(
                paddingValues = innerPadding,
                createMode = createMode,
                hapticFeedback = hapticFeedback,
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
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeReminder(reminder = null))
                },
                time = addEditTaskViewModel.selectedTime,
                onTimeClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog(
                        type = AddEditTaskEvent.DialogType.Time
                    ))
                },
                onTimeRemove = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeTime(time = null))
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeReminder(reminder = null))
                },
                reminder = addEditTaskViewModel.selectedReminder,
                onReminderTextFieldClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowReminders(show = true))
                },
                showReminderDropdown = addEditTaskViewModel.showReminders,
                onDismissReminderDropdown = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowReminders(show = false))
                },
                onReminderOptionClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowReminders(show = false))
                    addEditTaskViewModel.onEvent(
                        AddEditTaskEvent.ChangeReminder(
                            when (it) {
                                TaskReminderOption.FiveMinutes -> Reminder.FIVE_MINUTES
                                TaskReminderOption.TenMinutes -> Reminder.TEN_MINUTES
                                TaskReminderOption.FifteenMinutes -> Reminder.FIFTEEN_MINUTES
                                TaskReminderOption.ThirtyMinutes -> Reminder.THIRTY_MINUTES
                            }
                        )
                    )
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
    reorderableState: ReorderableLazyListState = rememberReorderableLazyListState(
        onMove = { _, _ -> }
    ),
    selectedBoardName: String = "",
    showBoardsMenu: Boolean = false,
    onBoardIndicatorClick: () -> Unit = {},
    nameState: TextFieldState = TextFieldState(),
    descriptionState: TextFieldState = TextFieldState(),
    date: LocalDate? = null,
    onDateClick: () -> Unit = {},
    onDateRemove: () -> Unit = {},
    time: LocalTime? = null,
    onTimeClick: () -> Unit = {},
    onTimeRemove: () -> Unit = {},
    reminder: Reminder? = null,
    onReminderTextFieldClick: () -> Unit = {},
    showReminderDropdown: Boolean = false,
    onDismissReminderDropdown: () -> Unit = {},
    onReminderOptionClick: (TaskReminderOption) -> Unit = {},
    priority: Priority? = null,
    onPriorityClick: (Priority) -> Unit = {},
    subtaskTextFields: List<TextFieldState> = emptyList(),
    onCreateSubtask: () -> Unit = {},
    onRemoveSubtask: (TextFieldState) -> Unit = {},
    onRemoveSubtaskByKeyPress: (position: Int, TextFieldState) -> Unit = {_, _ -> },
    onNextSubtask: (position: Int) -> Unit = {},
    onAddEditButtonClick: () -> Unit = {},
) {
    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
            state = reorderableState.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(reorderableState)
        ) {
            item(
                key = LazyListKeys.SELECTED_BOARD_INDICATOR,
                contentType = ContentTypes.BOARD_INDICATOR
            ) {
                SelectedBoardIndicator(
                    boardName = selectedBoardName,
                    expanded = showBoardsMenu,
                    onIndicatorClick = onBoardIndicatorClick,
                    modifier = Modifier.animateItemPlacement()
                )
                Spacer(modifier = Modifier
                    .height(16.dp)
                    .animateItemPlacement())
            }
            item(
                key = LazyListKeys.NAME_TEXT_FIELD,
                contentType = ContentTypes.TEXT_FIELDS
            ) {
                FinitoTextField(
                    value = nameState.value,
                    onValueChange = nameState.onValueChange,
                    label = { Text(text = stringResource(id = R.string.name)) },
                    modifier = Modifier.animateItemPlacement(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                )
                Spacer(modifier = Modifier
                    .height(24.dp)
                    .animateItemPlacement())
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
                    modifier = Modifier.animateItemPlacement(),
                )
                Spacer(modifier = Modifier
                    .height(24.dp)
                    .animateItemPlacement())
            }
            item(
                key = LazyListKeys.TASK_DATE_TEXT_FIELD,
                contentType = ContentTypes.TEXT_FIELDS
            ) {
                DateTextField(
                    date = date,
                    onClick = onDateClick,
                    onDateRemove = onDateRemove,
                    modifier = Modifier.animateItemPlacement()
                )
                Spacer(modifier = Modifier
                    .height(24.dp)
                    .animateItemPlacement())
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
                    modifier = Modifier.animateItemPlacement()
                )
                Spacer(modifier = Modifier
                    .height(24.dp)
                    .animateItemPlacement())
            }
            item(
                key = LazyListKeys.REMINDER_TEXT_FIELD,
                contentType = ContentTypes.TEXT_FIELDS
            ) {
                ReminderDropdownTextField(
                    selectedReminder = reminder,
                    enabled = time != null,
                    onReminderClick = onReminderTextFieldClick,
                    showDropdown = showReminderDropdown,
                    onDismissDropdown = onDismissReminderDropdown,
                    onOptionClick = onReminderOptionClick,
                    modifier = Modifier.animateItemPlacement()
                )
                Spacer(modifier = Modifier
                    .height(24.dp)
                    .animateItemPlacement())
            }

            item(
                key = LazyListKeys.TASK_PRIORITY,
                contentType = ContentTypes.PRIORITY_CHIPS
            ) {
                PriorityChips(
                    selectedPriority = priority,
                    onPriorityClick = onPriorityClick,
                    modifier = Modifier.animateItemPlacement()
                )
                Spacer(modifier = Modifier
                    .height(24.dp)
                    .animateItemPlacement())
            }

            item(
                key = LazyListKeys.SUBTASKS_TITLE,
                contentType = ContentTypes.SUBTASK_TITLE
            ) {
                Text(
                    text = stringResource(id = R.string.subtasks),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.animateItemPlacement()
                )
            }

            itemsIndexed(
                items = subtaskTextFields,
                key = { _, state -> state.id },
                contentType = { _, _ -> ContentTypes.SUBTASK_TEXT_FIELDS }
            ) { index, textFieldState ->
                ReorderableItem(reorderableState, key = textFieldState.id) { isDragging ->
                    SubtaskTextFieldItem(
                        state = textFieldState,
                        reorderableState = reorderableState,
                        hapticFeedback = hapticFeedback,
                        isDragging = isDragging,
                        onRemoveSubtask = { onRemoveSubtask(textFieldState) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { onNextSubtask(index) }
                        ),
                        modifier = Modifier.animateItemPlacement(),
                        textFieldModifier = Modifier
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
                    modifier = Modifier.animateItemPlacement()
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
                    modifier = Modifier.animateItemPlacement()
                ) { validName ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onAddEditButtonClick,
                            enabled = validName,
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
                    TextFieldState(id = 1, value = "Subtask 1"),
                    TextFieldState(id = 2, value = "Subtask 2"),
                    TextFieldState(id = 3),
                )
            )
        }
    }
}