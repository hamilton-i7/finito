package com.example.finito.features.tasks.presentation.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.finito.MainActivity
import com.example.finito.R
import com.example.finito.core.presentation.NOTIFICATION_LED_OFF_MS
import com.example.finito.core.presentation.NOTIFICATION_LED_ON_MS
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.RequestCodes
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.util.formatted

class TaskReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val TASK_REMINDER_CHANNEL_ID = "TASK_REMINDER_CHANNEL_ID"
        const val EXTRA_TASK = "TASK_EXTRA"
        const val TAG = "TaskReminderAlarmReceiver"
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
        val actionColor = when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> context.getColor(R.color.md_theme_dark_primary)
            else -> context.getColor(R.color.md_theme_light_primary)
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
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
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
        val notification = NotificationCompat.Builder(context, TASK_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(task.name)
            .setContentText(task.time!!.formatted())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(editTaskPendingIntent)
            .setLights(
                context.getColor(R.color.md_theme_light_primary),
                NOTIFICATION_LED_ON_MS,
                NOTIFICATION_LED_OFF_MS
            )
            .setColor(actionColor)
            .addAction(
                R.drawable.ic_baseline_done_all_24,
                context.getString(R.string.mark_as_completed),
                markTaskAsCompletedPendingIntent
            )
            .setAutoCancel(true)
            .let {
                if (task.description != null)
                    it.setStyle(NotificationCompat.BigTextStyle().bigText(task.description))
                else it
            }.build()
        notificationManager.notify(TAG, task.taskId, notification)
    }
}