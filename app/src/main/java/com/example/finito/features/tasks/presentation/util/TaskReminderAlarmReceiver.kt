package com.example.finito.features.tasks.presentation.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.finito.MainActivity
import com.example.finito.R
import com.example.finito.core.domain.Result
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.RequestCodes
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskReminderAlarmReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob())

    @Inject
    lateinit var taskUseCases: TaskUseCases

    companion object {
        const val TASK_REMINDER_CHANNEL_ID = "TASK_REMINDER_CHANNEL_ID"
        const val EXTRA_TASK_ID = "TASK_EXTRA"
        const val TAG = "TaskReminderAlarmReceiver"
        const val GROUP_KEY = "TASK_REMINDERS_GROUP"
        const val SUMMARY_NOTIFICATION_ID = 0
    }

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context, intent)
    }

    @Suppress("DEPRECATION")
    private fun showNotification(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
        scope.launch(Dispatchers.Default) {
            try {
                val result = taskUseCases.findOneTask(taskId)
                if (result is Result.Error) return@launch

                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val taskWithSubtasks = (result as Result.Success).data
                val taskNotification = createNotification(context, taskWithSubtasks.task)
                notificationManager.notify(TAG, taskWithSubtasks.task.taskId, taskNotification)

                val summaryNotification = createSummaryNotification(context, notificationManager)
                notificationManager.notify(TAG, SUMMARY_NOTIFICATION_ID, summaryNotification)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun createTaskPendingIntent(context: Context, taskId: Int): PendingIntent? {
        val editTaskIntent = Intent(
            Intent.ACTION_VIEW,
            "${Screen.URI}/${Screen.TASK_ID_ARGUMENT}=${taskId}".toUri(),
            context,
            MainActivity::class.java
        )
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(editTaskIntent)
            getPendingIntent(
                RequestCodes.EDIT_TASK_PENDING_INTENT_REQUEST_CODE + taskId,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private fun createMarkAsCompletedPendingIntent(context: Context, taskId: Int): PendingIntent? {
        val markTaskAsCompletedIntent = Intent(
            context,
            MarkTaskAsCompletedReceiver::class.java
        ).apply {
            putExtra(MarkTaskAsCompletedReceiver.EXTRA_TASK_ID, taskId)
        }

        return PendingIntent.getBroadcast(
            context,
            RequestCodes.MARK_TASK_AS_COMPLETED_REQUEST_CODE + taskId,
            markTaskAsCompletedIntent,
            PendingIntent.FLAG_IMMUTABLE or Intent.FILL_IN_DATA
        )
    }

    private fun createNotification(context: Context, task: Task): Notification {
        val editTaskPendingIntent = createTaskPendingIntent(context, task.taskId)
        val markTaskAsCompletedPendingIntent = createMarkAsCompletedPendingIntent(context, task.taskId)
        val name = SpannableStringBuilder(task.name).apply {
            setSpan(Typeface.BOLD, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val description = task.description?.let {
            SpannableString(it).apply {
                setSpan(
                    ForegroundColorSpan(context.getColor(R.color.md_theme_light_outline)),
                    0,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return NotificationCompat.Builder(context, TASK_REMINDER_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_round_done_all_24)
            setContentTitle(name)
            setContentIntent(editTaskPendingIntent)
            addAction(
                R.drawable.ic_round_done_all_24,
                context.getString(R.string.mark_as_completed),
                markTaskAsCompletedPendingIntent
            )
            setAutoCancel(true)
            setCategory(NotificationCompat.CATEGORY_REMINDER)
            if (description != null) {
                setContentText(description)
                setStyle(NotificationCompat.BigTextStyle().bigText(description))
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                color = context.getColor(R.color.md_theme_light_primary)
            }
            setGroup(GROUP_KEY)
        }.build()
    }

    private fun createSummaryNotification(
        context: Context,
        notificationManager: NotificationManager,
    ): Notification {
        return NotificationCompat.Builder(context, TASK_REMINDER_CHANNEL_ID).apply {
            val activeNotifications = notificationManager.activeNotifications.filter {
                it.notification.group == GROUP_KEY && it.id != SUMMARY_NOTIFICATION_ID
            }
            val summary = context.resources.getQuantityString(
                R.plurals.plural_task_due,
                activeNotifications.size,
                activeNotifications.size
            )

            setSmallIcon(R.drawable.ic_round_done_all_24)
            setStyle(NotificationCompat.InboxStyle().setSummaryText(summary))
            setGroup(GROUP_KEY)
            setGroupSummary(true)
        }.build()
    }
}