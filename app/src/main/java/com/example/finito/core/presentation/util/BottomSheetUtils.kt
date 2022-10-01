package com.example.finito.core.presentation.util

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
fun calculateDp(bottomSheetState: ModalBottomSheetState): Dp {
    if (bottomSheetState.currentValue == ModalBottomSheetValue.Hidden) return 28.dp
    if (bottomSheetState.currentValue == ModalBottomSheetValue.HalfExpanded) {
        return when (bottomSheetState.direction) {
            -1f -> 28.dp.times(other = 1 - bottomSheetState.progress.fraction)
            else -> 28.dp
        }
    }
    return when (bottomSheetState.direction) {
        -1f, 0f -> 0.dp
        else -> {
            if (bottomSheetState.progress.to == ModalBottomSheetValue.Expanded) 0.dp
            else 28.dp.times(bottomSheetState.progress.fraction)
        }
    }
}