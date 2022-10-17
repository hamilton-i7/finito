package com.example.finito.core.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R

@Composable
fun CreateFab(
    @StringRes text: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
) {
    ExtendedFloatingActionButton(
        text = { Text(text = stringResource(id = text)) },
        icon = { Icon(painter = painterResource(id = R.drawable.plus_math), contentDescription = null) },
        onClick = onClick,
        expanded = expanded,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 2.dp,
            hoveredElevation = 2.dp,
            focusedElevation = 2.dp,
            pressedElevation = 1.dp,

        ),
        modifier = Modifier
            .navigationBarsPadding()
            .then(modifier)
    )
}