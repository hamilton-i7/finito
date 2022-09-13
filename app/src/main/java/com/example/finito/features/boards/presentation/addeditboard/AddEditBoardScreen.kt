package com.example.finito.features.boards.presentation.addeditboard

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
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
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.components.bars.TopBar
import com.example.finito.core.presentation.components.textfields.FinitoTextField
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.LabelItem
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBoardScreen(
    navController: NavController,
    addEditBoardViewModel: AddEditBoardViewModel = hiltViewModel(),
) {
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        addEditBoardViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditBoardViewModel.UiEvent.CreateBoard -> {
                    val route = "${Screen.Board.prefix}/${event.boardId}"
                    navController.navigate(route = route) {
                        popUpTo(Screen.CreateBoard.route) { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = R.string.create_board,
                navigationIcon = Icons.Outlined.ArrowBack,
                navigationIconDescription = R.string.go_back,
                scrollBehavior = topBarScrollBehavior,
                onNavigationIconClick = { navController.navigateUp() },
            )
        },
        modifier = Modifier
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .noRippleClickable { focusManager.clearFocus() }
    ) { innerPadding ->
        AddEditBoardScreen(
            paddingValues = innerPadding,
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
            onCreateBoard = {
                addEditBoardViewModel.onEvent(AddEditBoardEvent.CreateBoard)
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AddEditBoardScreen(
    paddingValues: PaddingValues = PaddingValues(),
    nameState: TextFieldState = TextFieldState(),
    nameFieldEnabled: Boolean = true,
    showLabels: Boolean = true,
    onShowLabelsChange: () -> Unit = {},
    labels: List<SimpleLabel> = emptyList(),
    selectedLabels: List<SimpleLabel> = emptyList(),
    onLabelClick: (SimpleLabel) -> Unit = {},
    onCreateBoard: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    val selectedLabelsIds = selectedLabels.groupBy { it.labelId }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(contentType = { "name text field" }) {
                FinitoTextField(
                    value = nameState.value,
                    onValueChange = nameState.onValueChange,
                    enabled = nameFieldEnabled,
                    label = { Text(text = stringResource(id = R.string.name)) },
                    error = nameState.error,
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
                        onClick = onCreateBoard,
                        enabled = validName,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .widthIn(max = 350.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.create_board))
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
                nameState = TextFieldState(error = true),
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