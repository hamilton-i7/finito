package com.example.finito.core.presentation.util

import androidx.navigation.NavController
import com.example.finito.core.presentation.Screen
import com.example.finito.features.boards.domain.entity.BoardState
import java.time.LocalDate

fun NavController.navigateToHome() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Home.route) { inclusive = true }
    }
}

fun NavController.navigateToLabel(labelId: Int) {
    val route = "${Screen.Label.prefix}/$labelId"
    navigate(route) {
        popUpTo(Screen.Board.route) { inclusive = true }
    }
}

fun NavController.navigateToBoardFlow(boardId: Int, state: BoardState = BoardState.ACTIVE) {
    val route = "${Screen.Board.prefix}/$boardId" +
            "?${Screen.BOARD_STATE_ARGUMENT}" +
            "=${state.name}"
    navigate(route)
}

fun NavController.navigateToBoard(boardId: Int, state: BoardState = BoardState.ACTIVE) {
    val route = "${Screen.Board.prefix}/$boardId" +
            "?${Screen.BOARD_STATE_ARGUMENT}" +
            "=${state.name}"
    navigate(route) {
        popUpTo(Screen.Board.route) { inclusive = true }
    }
}

fun NavController.navigateToCreateBoard() {
    navigate(route = Screen.CreateBoard.route)
}

fun NavController.navigateToEditBoard(
    boardId: Int,
    state: BoardState = BoardState.ACTIVE
) {
    val route = "${Screen.EditBoard.prefix}/$boardId" +
            "?${Screen.BOARD_STATE_ARGUMENT}=${state.name}"
    navigate(route)
}

fun NavController.navigateToCreateTask(boardId: Int, isUrgent: Boolean = false) {
    val route = "${Screen.CreateTask.prefix}" +
            "?${Screen.BOARD_ID_ARGUMENT}=$boardId" +
            "&${Screen.IS_URGENT_ARGUMENT}=$isUrgent"
    navigate(route)
}

fun NavController.navigateToCreateTask(boardId: Int, date: LocalDate) {
    val route = "${Screen.CreateTask.prefix}" +
            "?${Screen.BOARD_ID_ARGUMENT}=$boardId" +
            "&${Screen.DATE_ARGUMENT}=$date"
    navigate(route)
}

fun NavController.navigateToCreateTask(boardId: Int, taskName: String, isUrgent: Boolean = false) {
    val route = "${Screen.CreateTask.prefix}" +
            "?${Screen.BOARD_ID_ARGUMENT}=$boardId" +
            "&${Screen.TASK_NAME_ARGUMENT}=$taskName" +
            "&${Screen.IS_URGENT_ARGUMENT}=$isUrgent"
    navigate(route)
}

fun NavController.navigateToCreateTask(
    boardId: Int,
    taskName: String,
    date: LocalDate,
) {
    val route = "${Screen.CreateTask.prefix}" +
            "?${Screen.BOARD_ID_ARGUMENT}=$boardId" +
            "&${Screen.TASK_NAME_ARGUMENT}=$taskName" +
            "&${Screen.DATE_ARGUMENT}=$date"
    navigate(route)
}

fun NavController.navigateToEditTask(taskId: Int) {
    val route = "${Screen.EditTask.prefix}/${taskId}"
    navigate(route)
}

fun NavController.navigateToEditSubtask(boardId: Int, subtaskId: Int) {
    val route = "${Screen.EditSubtask.prefix}/${subtaskId}?${Screen.BOARD_ID_ARGUMENT}=$boardId"
    navigate(route)
}

fun NavController.navigateBackTwice() {
    navigateUp()
    navigateUp()
}