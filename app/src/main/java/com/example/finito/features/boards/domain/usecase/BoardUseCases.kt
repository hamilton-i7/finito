package com.example.finito.features.boards.domain.usecase

data class BoardUseCases(
    val createBoard: CreateBoard,
    val findActiveBoards: FindActiveBoards,
    val findArchivedBoards: FindArchivedBoards,
    val findDeletedBoards: FindDeletedBoards,
    val findSimpleBoards: FindSimpleBoards,
    val findOneBoard: FindOneBoard,
    val updateBoard: UpdateBoard,
    val deleteBoard: DeleteBoard,
    val arrangeBoards: ArrangeBoards
)
