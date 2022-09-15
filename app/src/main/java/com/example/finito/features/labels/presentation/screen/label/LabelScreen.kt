package com.example.finito.features.labels.presentation.screen.label

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelScreen(
    navController: NavController,
    drawerState: DrawerState,
    showSnackbar: (message: Int, actionLabel: Int?, onActionClick: () -> Unit) -> Unit,
    labelViewModel: LabelViewModel = hiltViewModel(),
) {}

@Composable
private fun LabelScreen(
    paddingValues: PaddingValues = PaddingValues()
) {}