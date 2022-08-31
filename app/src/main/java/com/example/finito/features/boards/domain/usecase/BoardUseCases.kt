package com.example.finito.features.boards.domain.usecase

data class BoardUseCases(
    val createBoard: CreateBoard,
    val createBoardWithLabels: CreateBoardWithLabels,
    val createBoardLabel: CreateBoardLabel,
    val findAllBoards: FindAllBoards,
    val findArchivedBoards: FindArchivedBoards,
    val findDeletedBoards: FindDeletedBoards,
    val findSimpleBoards: FindSimpleBoards
)
