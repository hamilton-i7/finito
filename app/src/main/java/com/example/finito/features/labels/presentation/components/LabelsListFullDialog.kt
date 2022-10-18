package com.example.finito.features.labels.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.util.Keyboard
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.core.presentation.util.keyboardAsState
import com.example.finito.features.labels.domain.entity.SimpleLabel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun LabelsListFullDialog(
    labels: List<SimpleLabel>,
    selectedLabels: List<SimpleLabel> = emptyList(),
    onLabelClick: (SimpleLabel) -> Unit = {},
    searchQuery: TextFieldState = TextFieldState.Default,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onCloseClick: () -> Unit = {},
) {
    val selectedLabelsIds = selectedLabels.groupBy { it.labelId }
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardState by keyboardAsState()

    BackHandler {
        if (keyboardState == Keyboard.CLOSED) {
            onCloseClick()
            return@BackHandler
        }
        keyboardController?.hide()
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                queryState = searchQuery,
                onBackClick = onBackClick@{
                    if (keyboardState == Keyboard.CLOSED) {
                        onCloseClick()
                        return@onBackClick
                    }
                    keyboardController?.hide()
                },
                scrollBehavior = scrollBehavior,
                placeholder = stringResource(id = R.string.search_labels),
                backIconDescription = R.string.close_dialog,
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(modifier = Modifier.imePadding()) {
                items(
                    items = labels,
                    key = { it.labelId }
                ) { label ->
                    LabelItem(
                        label = label,
                        selected = selectedLabelsIds[label.labelId] != null,
                        onLabelClick = { onLabelClick(label) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}