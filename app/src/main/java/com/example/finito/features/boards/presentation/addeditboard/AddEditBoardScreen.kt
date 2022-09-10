package com.example.finito.features.boards.presentation.addeditboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun AddEditBoardScreen(
    navController: NavController,
    addEditBoardViewModel: AddEditBoardViewModel = hiltViewModel(),
) {

}

@Composable
private fun AddEditBoardScreen(
    paddingValues: PaddingValues = PaddingValues(),
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column {

        }
    }
}