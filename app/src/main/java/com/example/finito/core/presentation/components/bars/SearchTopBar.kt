package com.example.finito.core.presentation.components.bars

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.finito.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    onBackClick: () -> Unit = {},
    query: String,
    onQueryChange: (String) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TopAppBar(title = {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(text = stringResource(id = R.string.search_boards))
            },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(id = R.string.close_search_bar)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}