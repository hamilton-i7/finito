package com.example.finito.features.boards.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors

@Composable
fun SortBoardsSheetContent(
    selectedOption: SortingOption.Common = SortingOption.Common.Default,
    onClick: (SortingOption.Common) -> Unit = {},
) {
    val options = listOf(
        SortingOption.Common.Newest,
        SortingOption.Common.Oldest,
        SortingOption.Common.NameAZ,
        SortingOption.Common.NameZA,
        SortingOption.Common.Custom,
    )

    Surface(tonalElevation = 1.dp) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 22.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = finitoColors.onSurfaceVariant.copy(alpha = 0.4f))
            )
            Text(
                text = stringResource(id = R.string.sort_by),
                style = MaterialTheme.typography.bodySmall,
                color = finitoColors.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            options.forEach { option ->
                SheetContentItem(
                    selected = selectedOption == option,
                    text = option.label,
                    onClick = { onClick(option) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetContentItem(
    selected: Boolean,
    @StringRes text: Int,
    onClick: () -> Unit = {},
) {
    ListItem(
        headlineText = {
            Text(
                text = stringResource(id = text),
                color = if (selected) finitoColors.primary else finitoColors.onSurface
            )
        },
        trailingContent = trailingContent@{
            if (!selected) return@trailingContent
            Icon(
                painter = painterResource(id = R.drawable.done),
                contentDescription = stringResource(id = R.string.selected),
                tint = finitoColors.primary
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Preview
@Composable
private fun SortBoardsSheetContentPreview() {
    FinitoTheme {
        Surface {
            SortBoardsSheetContent()
        }
    }
}