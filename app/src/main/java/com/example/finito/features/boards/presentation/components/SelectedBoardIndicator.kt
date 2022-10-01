package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.ui.theme.FinitoTheme

@Composable
fun SelectedBoardIndicator(
    boardName: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onIndicatorClick: () -> Unit = {},
) {
    TextButton(
        onClick = onIndicatorClick,
        modifier = modifier
    ) {
        Text(
            text = boardName,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Outlined.ArrowDropDown,
            contentDescription = stringResource(
                id = if (expanded) R.string.hide_boards_menu else R.string.show_boards_menu
            ),
        )
    }
}

@Preview
@Composable
fun SelectedBoardIndicatorPreview() {
    FinitoTheme {
        SelectedBoardIndicator(
            boardName = "Board name",
        )
    }
}