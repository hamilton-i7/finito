package com.example.finito.features.tasks.presentation.util

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
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.RequestCodes
import com.example.finito.features.tasks.domain.entity.Task

class TaskReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val TASK_REMINDER_CHANNEL_ID = "TASK_REMINDER_CHANNEL_ID"
        const val EXTRA_TASK = "TASK_EXTRA"
        const val TAG = "TaskReminderAlarmReceiver"
        const val GROUP_KEY = "TASK_REMINDERS_GROUP"
        const val SUMMARY_NOTIFICATION_ID = 0
    }

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context, intent)
    }

    @Suppress("DEPRECATION")
    private fun showNotification(context: Context, intent: Intent) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TASK, Task::class.java)!!
        } else {
            intent.getParcelableExtra(EXTRA_TASK)!!
        }
        val editTaskIntent = Intent(
            Intent.ACTION_VIEW,
            "${Screen.URI}/${Screen.TASK_ID_ARGUMENT}=${task.taskId}".toUri(),
            context,
            MainActivity::class.java
        )
        val editTaskPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(editTaskIntent)
            getPendingIntent(
                RequestCodes.EDIT_TASK_PENDING_INTENT_REQUEST_CODE + task.taskId,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val markTaskAsCompletedIntent = Intent(
            context,
            MarkTaskAsCompletedReceiver::class.java
        ).apply {
            putExtra(MarkTaskAsCompletedReceiver.EXTRA_TASK_ID, task.taskId)
        }
        val markTaskAsCompletedPendingIntent = PendingIntent.getBroadcast(
            context,
            RequestCodes.MARK_TASK_AS_COMPLETED_REQUEST_CODE + task.taskId,
            markTaskAsCompletedIntent,
            PendingIntent.FLAG_IMMUTABLE or Intent.FILL_IN_DATA
        )
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

        val taskNotification = NotificationCompat.Builder(context, TASK_REMINDER_CHANNEL_ID).apply {
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
        notificationManager.notify(TAG, task.taskId, taskNotification)

        val summaryNotification = NotificationCompat.Builder(context, TASK_REMINDER_CHANNEL_ID).apply {
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

        notificationManager.notify(TAG, SUMMARY_NOTIFICATION_ID, summaryNotification)
    }
}