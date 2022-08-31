package com.example.finito.features.boards.domain.usecase

data class BoardUseCases(
    val createBoard: CreateBoard,
    val findAllBoards: FindAllBoards,
    val findArchivedBoards: FindArchivedBoards,
    val findDeletedBoards: FindDeletedBoards,
    val findSimpleBoards: FindSimpleBoards,
    val findOneBoard: FindOneBoard
)
