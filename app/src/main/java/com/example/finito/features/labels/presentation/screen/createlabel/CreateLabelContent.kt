package com.example.finito.features.labels.presentation.screen.createlabel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLabelContent(
    createLabelViewModel: CreateLabelViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onShowSnackbar: (Int, Int?, () -> Unit) -> Unit = {_, _, _ ->},
) {
    val (name, onNameChange) = createLabelViewModel.nameState.copy(
        onValueChange = {
            createLabelViewModel.onEvent(CreateLabelEvent.ChangeName(it))
        }
    )

    LaunchedEffect(Unit) {
        createLabelViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is CreateLabelViewModel.Event.ShowSnackbar -> {
                    onShowSnackbar(event.message, null) {}
                    onNavigateBack()
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.testTag(TestTags.CREATE_LABEL_DIALOG)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = finitoColors.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.create_label),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                label = { Text(text = stringResource(id = R.string.name)) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    unfocusedLabelColor = finitoColors.onSurfaceVariant.copy(alpha = 0.60f)
                ),
                modifier = Modifier.testTag(TestTags.NAME_TEXT_FIELD)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onNavigateBack) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = {
                        createLabelViewModel.onEvent(CreateLabelEvent.CreateLabel)
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.testTag(TestTags.DIALOG_CONFIRM_BUTTON)
                ) { Text(text = stringResource(id = R.string.create)) }
            }
        }
    }
}