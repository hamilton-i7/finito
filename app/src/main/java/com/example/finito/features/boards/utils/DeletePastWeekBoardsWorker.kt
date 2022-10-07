package com.example.finito.features.boards.utils

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class DeletePastWeekBoardsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val boardUseCases: BoardUseCases
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val boards = findBoardsToDelete()
        boardUseCases.deleteBoard(*boards.toTypedArray())
        return Result.success()
    }

    private suspend fun findBoardsToDelete(): List<Board> {
        return boardUseCases.findDeletedBoardsAsync().data.let { boards ->
            val trashLimit = LocalDate.now().minusDays(6)
            boards.filter { it.removedAt!!.toLocalDate().isBefore(trashLimit) }.takeLast(2)
        }.also { Log.d(TAG, it.toString()) }
    }

    companion object {
        const val MINIMUM_INTERVALS = 24L
        const val TAG = "DELETE_PAST_WEEK_BOARDS_WORKER"
    }
}