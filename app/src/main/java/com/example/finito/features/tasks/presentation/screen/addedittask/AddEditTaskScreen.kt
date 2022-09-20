package com.example.finito.features.tasks.presentation.screen.addedittask

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
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
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.presentation.components.SelectedBoardIndicator
import com.example.finito.features.subtasks.presentation.components.SubtaskTextFieldItem
import com.example.finito.features.tasks.presentation.screen.addedittask.components.AddEditTaskTopBar
import com.example.finito.features.tasks.presentation.screen.addedittask.components.ReminderDropdownTextField
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.launch
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
                contentPadding = PaddingValues(vertical = 32.dp),
                modifier = Modifier.navigationBarsPadding()
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.choose_board),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(addEditTaskViewModel.boards) { board -> 
                    ListItem(
                        headlineText = { Text(text = board.name) },
                        leadingContent = {
                            Icon(imageVector = Icons.Outlined.NoteAlt, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeBoard(board))
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
            modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
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
                reminder = addEditTaskViewModel.selectedReminder,
                onReminderTextFieldClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowReminders(show = true))
                },
                showReminderDropdown = addEditTaskViewModel.showReminders,
                onDismissReminderDropdown = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowReminders(show = false))
                },
                onReminderOptionClick = {
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
                onPriorityClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangePriority(it))
                },
                subtaskTextFields = addEditTaskViewModel.subtaskNameStates,
                onCreateSubtask = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.CreateSubtask)
                },
                onRemoveSubtask = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.RemoveSubtask(it))
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
    ExperimentalAnimationApi::class
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
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item(
                key = LazyListKeys.NAME_TEXT_FIELD,
                contentType = ContentTypes.TEXT_FIELDS
            ) {
                FinitoTextField(
                    value = nameState.value,
                    onValueChange = nameState.onValueChange,
                    label = { Text(text = stringResource(id = R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
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
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            item(
                key = LazyListKeys.TASK_DATE_TIME_TEXT_FIELDS,
                contentType = ContentTypes.TEXT_FIELDS
            ) {
                Row {
                    DateTextField(
                        date = date,
                        onClick = onDateClick,
                        onDateRemove = onDateRemove,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TimeTextField(
                        time = time,
                        onClick = onTimeClick,
                        onTimeRemove = onTimeRemove,
                        enabled = date != null,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
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
                    onOptionClick = onReminderOptionClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item(
                key = LazyListKeys.TASK_PRIORITY,
                contentType = ContentTypes.PRIORITY_CHIPS
            ) {
                PriorityChips(
                    selectedPriority = priority,
                    onPriorityClick = onPriorityClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item(
                key = LazyListKeys.SUBTASKS_TITLE,
                contentType = ContentTypes.SUBTASK_TITLE
            ) {
                Text(
                    text = stringResource(id = R.string.subtasks),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(
                items = subtaskTextFields,
                key = { it.id },
                contentType = { ContentTypes.SUBTASK_TEXT_FIELDS }
            ) { textFieldState ->
                SubtaskTextFieldItem(
                    state = textFieldState,
                    reorderableState = reorderableState,
                    hapticFeedback = hapticFeedback,
                    onRemoveSubtask = { onRemoveSubtask(textFieldState) },
                    modifier = Modifier.animateItemPlacement()
                )
            }

            item(
                key = LazyListKeys.CREATE_SUBTASK_BUTTON,
                contentType = ContentTypes.SECONDARY_BUTTON
            ) {
                TextButton(onClick = onCreateSubtask) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.create_subtask))
                }
            }

            item(key = LazyListKeys.PRIMARY_BUTTON) {
                Spacer(modifier = Modifier.height(40.dp))
                AnimatedContent(
                    targetState = nameState.value.isNotBlank(),
                    transitionSpec = { fadeIn() with fadeOut() }
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