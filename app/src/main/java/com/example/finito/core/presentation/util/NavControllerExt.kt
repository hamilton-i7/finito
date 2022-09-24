package com.example.finito.core.presentation.util

import androidx.navigation.NavController
import com.example.finito.core.presentation.Screen
import com.example.finito.features.boards.domain.entity.BoardState

fun NavController.navigateToHome() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Home.route) { inclusive = true }
    }
}

fun NavController.navigateToArchive() {
    navigate(Screen.Archive.route) {
        popUpTo(Screen.Archive.route) { inclusive = true }
    }
}

fun NavController.navigateToTrash() {
    navigate(route = Screen.Trash.route) {
        popUpTo(Screen.Trash.route) { inclusive = true }
    }
}

fun NavController.navigateToBoardFlow(boardId: Int, state: BoardState = BoardState.ACTIVE) {
    val route = "${Screen.Board.prefix}/${boardId}" +
            "?${Screen.BOARD_ROUTE_STATE_ARGUMENT}" +
            "=${state.name}"
    navigate(route)
}

fun NavController.navigateToBoard(boardId: Int, state: BoardState = BoardState.ACTIVE) {
    val route = "${Screen.Board.prefix}/${boardId}" +
            "?${Screen.BOARD_ROUTE_STATE_ARGUMENT}" +
            "=${state.name}"
    navigate(route) {
        popUpTo(Screen.Board.route) { inclusive = true }
    }
}

fun NavController.navigateToCreateBoard() {
    navigate(route = Screen.CreateBoard.route)
}

fun NavController.navigateToEditBoard(boardId: Int, state: BoardState = BoardState.ACTIVE) {
    val route = "${Screen.EditBoard.prefix}/${boardId}" +
            "?${Screen.BOARD_ROUTE_STATE_ARGUMENT}" +
            "=${state.name}"
    navigate(route)
}

fun NavController.navigateToCreateTask(boardId: Int, includeTodayDate: Boolean = false) {
    val route = if (includeTodayDate) {
        "${Screen.CreateTask.prefix}" +
                "?${Screen.BOARD_ROUTE_ID_ARGUMENT}=$boardId" +
                "&${Screen.INCLUDE_TODAY_DATE_ARGUMENT}=true"
    } else {
        "${Screen.CreateTask.prefix}?${Screen.BOARD_ROUTE_ID_ARGUMENT}=$boardId"
    }
    navigate(route)
}

fun NavController.navigateToCreateTask(
    boardId: Int,
    taskName: String,
    includeTodayDate: Boolean = false,
) {
    val route = if (includeTodayDate) {
        "${Screen.CreateTask.prefix}" +
                "?${Screen.BOARD_ROUTE_ID_ARGUMENT}=$boardId" +
                "&${Screen.TASK_NAME_ARGUMENT}=$taskName" +
                "&${Screen.INCLUDE_TODAY_DATE_ARGUMENT}=true"
    } else {
        "${Screen.CreateTask.prefix}" +
                "?${Screen.BOARD_ROUTE_ID_ARGUMENT}=$boardId" +
                "&${Screen.TASK_NAME_ARGUMENT}=true"
    }
    navigate(route)
}

fun NavController.navigateToEditTask(taskId: Int) {
    val route = "${Screen.EditTask.prefix}/${taskId}"
    navigate(route)
}