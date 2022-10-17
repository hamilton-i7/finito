package com.example.finito.core.presentation.components.bars

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.core.presentation.util.TextFieldState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    onBackClick: () -> Unit = {},
    queryState: TextFieldState,
    @StringRes placeholder: Int = R.string.search_boards,
    @StringRes backIconDescription: Int = R.string.close_search_bar,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    focusRequester: FocusRequester? = null,
) {
    LaunchedEffect(Unit) {
        focusRequester?.requestFocus()
    }

    TopAppBar(title = {
        OutlinedTextField(
            value = queryState.value,
            onValueChange = queryState.onValueChange,
            placeholder = {
                Text(text = stringResource(id = placeholder))
            },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.SEARCH_TEXT_FIELD)
                .then(
                    other = focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
                )
        )
    },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = stringResource(id = backIconDescription)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}