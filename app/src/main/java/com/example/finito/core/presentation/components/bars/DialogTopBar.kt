package com.example.finito.core.presentation.components.bars

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogTopBar(
    onCloseClick: () -> Unit = {},
    @StringRes title: Int,
    saveButtonEnabled: Boolean = true,
    onSave: () -> Unit = {},
) {
    TopAppBar(title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(id = R.string.close_dialog)
                )
            }
        },
        actions = {
            TextButton(onClick = onSave, enabled = saveButtonEnabled) {
                Text(text = stringResource(id = R.string.save))
            }
        }, colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = finitoColors.surfaceColorAtElevation(3.dp)
        ))
}