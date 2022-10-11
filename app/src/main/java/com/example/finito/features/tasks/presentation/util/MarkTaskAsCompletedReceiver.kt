package com.example.finito.features.tasks.presentation.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.finito.core.domain.Result
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class MarkTaskAsCompletedReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob())

    @Inject
    lateinit var taskUseCases: TaskUseCases

    companion object {
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        private const val TAG = "MarkTaskAsCompletedReceiver"
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        scope.launch(Dispatchers.Default) {
            try {
                when (val result = taskUseCases.findOneTask(taskId)) {
                    is Result.Error -> Log.e(TAG, result.message)
                    is Result.Success -> {
                        taskUseCases.toggleTaskCompleted(
                            result.data.copy(
                                task = result.data.task.copy(
                                    completed = true,
                                    completedAt = LocalDateTime.now(),
                                    boardPosition = null
                                )
                            )
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
        notificationManager.cancel(TaskReminderAlarmReceiver.TAG, taskId)
    }
}