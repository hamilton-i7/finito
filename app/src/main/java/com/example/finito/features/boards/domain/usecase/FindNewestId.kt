package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.domain.repository.BoardRepository

class FindNewestId(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(): Int {
        return repository.findNewestId()
    }
}