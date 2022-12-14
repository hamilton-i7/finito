package com.example.finito.core.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.FinitoTheme

private data class NavigationItem(
    val icon: ImageVector,
    @StringRes val label: Int,
    val screen: Screen,
)

private val mainScreens = listOf(
    NavigationItem(
        icon = Icons.Outlined.Home,
        label = R.string.home,
        screen = Screen.Home
    ),
    NavigationItem(
        icon = Icons.Outlined.Today,
        label = R.string.today,
        screen = Screen.Today
    ),
    NavigationItem(
        icon = Icons.Outlined.DateRange,
        label = R.string.tomorrow,
        screen = Screen.Tomorrow
    ),
    NavigationItem(
        icon = Icons.Outlined.LabelImportant,
        label = R.string.urgent,
        screen = Screen.Urgent
    ),
)

private val otherScreens = listOf(
    NavigationItem(
        icon = Icons.Outlined.Archive,
        label = R.string.archive,
        screen = Screen.Archive
    ),
    NavigationItem(
        icon = Icons.Outlined.Delete,
        label = R.string.trash,
        screen = Screen.Trash
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Drawer(
    drawerState: DrawerState,
    isSelectedScreen: (String) -> Boolean,
    boards: List<SimpleBoard> = emptyList(),
    onBoardSelected: (boardId: Int) -> Unit = {},
    labels: List<SimpleLabel> = emptyList(),
    onLabelSelected: (labelId: Int) -> Unit = {},
    expandBoards: Boolean = true,
    onExpandBoardsChange: () -> Unit = {},
    expandLabels: Boolean = true,
    onExpandLabelsChange: () -> Unit = {},
    onStaticItemSelected: (route: String) -> Unit = {},
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContent(
                isSelectedScreen,
                boards,
                onBoardSelected,
                labels,
                onLabelSelected,
                expandBoards,
                onExpandBoardsChange,
                expandLabels,
                onExpandLabelsChange,
                onStaticItemSelected
            )
        },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(
    isSelectedScreen: (route: String) -> Boolean,
    boards: List<SimpleBoard> ,
    onBoardSelected: (boardId: Int) -> Unit = {},
    labels: List<SimpleLabel>,
    onLabelSelected: (labelId: Int) -> Unit = {},
    expandBoards: Boolean,
    onExpandBoardsChange: () -> Unit,
    expandLabels: Boolean,
    onExpandLabelsChange: () -> Unit,
    onStaticItemSelected: (route: String) -> Unit = {},
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(
            topEnd = 32.dp,
            bottomEnd = 32.dp
        )
    ) {
        LazyColumn(contentPadding = PaddingValues(vertical = 24.dp)) {
            item {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(
                        vertical = 12.dp,
                        horizontal = 32.dp
                    )
                )
            }
            item { DrawerSectionHeader(text = R.string.main) }

            items(mainScreens) { screen ->
                NavigationDrawerItem(
                    icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                    label = { Text(text = stringResource(id = screen.label)) },
                    selected = isSelectedScreen(screen.screen.route),
                    onClick = { onStaticItemSelected(screen.screen.route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
            item { FinitoDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 32.dp)) }

            item {
                DrawerSectionHeader(
                    text = R.string.boards,
                    onExpandChange = onExpandBoardsChange,
                    expanded = expandBoards
                )
            }
            if (expandBoards) {
                items(boards) { board ->
                    val route = "${Screen.Board.prefix}/${board.boardId}"

                    NavigationDrawerItem(
                        icon = { Icon(imageVector = Icons.Outlined.NoteAlt, contentDescription = null) },
                        label = { Text(text = board.name) },
                        selected = isSelectedScreen(route),
                        onClick = { onBoardSelected(board.boardId) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
            item {
                DrawerItemButton(
                    text = R.string.create_new_board,
                    onClick = {
                        onStaticItemSelected(Screen.CreateBoard.route)
                    }
                )
            }
            item { FinitoDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 32.dp)) }

            item {
                DrawerSectionHeader(
                    text = R.string.labels,
                    onExpandChange = onExpandLabelsChange,
                    expanded = expandLabels,
                    showContentDescription = R.string.show_labels,
                    hideContentDescription = R.string.hide_labels
                )
            }
            if (expandLabels) {
                items(labels) { label ->
                    val route = "${Screen.Label.prefix}/${label.labelId}"

                    NavigationDrawerItem(
                        icon = { Icon(imageVector = Icons.Outlined.Label, contentDescription = null) },
                        label = { Text(text = label.name) },
                        selected = isSelectedScreen(route),
                        onClick = { onLabelSelected(label.labelId) },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag(TestTags.DRAWER_LABEL_ITEM)
                    )
                }
            }
            item {
                DrawerItemButton(text = R.string.create_new_label) {
                    onStaticItemSelected(Screen.CreateLabel.route)
                }
            }
            item { FinitoDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 32.dp)) }

            items(otherScreens) { screen ->
                NavigationDrawerItem(
                    icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                    label = { Text(text = stringResource(id = screen.label)) },
                    selected = isSelectedScreen(screen.screen.route),
                    onClick = { onStaticItemSelected(screen.screen.route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerSectionHeader(
    @StringRes text: Int,
    expanded: Boolean = false,
    onExpandChange: (() -> Unit)? = null,
    showContentDescription: Int? = null,
    hideContentDescription: Int? = null,
) {
    if (onExpandChange == null) {
        Text(
            text = stringResource(id = text),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(
                vertical = 12.dp,
                horizontal = 32.dp
            )
        )
        return
    }
    NavigationDrawerItem(
        label = {
            Text(
                text = stringResource(id = text),
                style = MaterialTheme.typography.titleSmall,
            )
        },
        badge = {
            if (expanded) {
                Icon(
                    imageVector = Icons.Outlined.ExpandLess,
                    contentDescription = hideContentDescription?.let {
                        stringResource(id = it)
                    }
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = showContentDescription?.let {
                        stringResource(id = it)
                    }
                )
            }
        },
        selected = false,
        onClick = onExpandChange,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerItemButton(
    @StringRes text: Int,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        icon = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null) },
        label = { Text(text = stringResource(id = text)) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun DrawerPreview() {
    FinitoTheme {
        Drawer(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
            isSelectedScreen = { route -> route == Screen.Home.route },
            boards = listOf(
                SimpleBoard(boardId = 1, name = "Board 1"),
                SimpleBoard(boardId = 2, name = "Board 2"),
                SimpleBoard(boardId = 3, name = "Board 3"),
                SimpleBoard(boardId = 4, name = "Board 4"),
            ),
            labels = listOf(
                SimpleLabel(labelId = 1, name = "Label 1"),
                SimpleLabel(labelId = 2, name = "Label 2"),
                SimpleLabel(labelId = 3, name = "Label 3"),
            ),
        ) {}
    }
}