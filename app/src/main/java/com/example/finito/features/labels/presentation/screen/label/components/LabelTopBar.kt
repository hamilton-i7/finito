package com.example.finito.features.labels.presentation.screen.label.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.example.finito.core.presentation.components.bars.MediumTopBarWithMenu
import com.example.finito.core.presentation.util.menu.LabelMenuOption
import com.example.finito.core.presentation.util.menu.LabelScreenMenuOption

private val options = listOf(
    LabelMenuOption.RenameLabel,
    LabelMenuOption.DeleteLabel,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelTopBar(
    labelName: String,
    onNavigationClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    onOptionClick: (LabelScreenMenuOption) -> Unit = {}
) {
    MediumTopBarWithMenu(
        title = labelName,
        onNavigationIconClick = onNavigationClick,
        onMoreOptionsClick = onMoreOptionsClick,
        scrollBehavior = scrollBehavior,
        showMenu = showMenu,
        onDismissMenu = onDismissMenu,
        options = options,
        onOptionClick = onOptionClick
    )
}