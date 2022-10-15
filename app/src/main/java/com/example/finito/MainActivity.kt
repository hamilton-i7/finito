package com.example.finito

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.finito.core.presentation.App
import com.example.finito.features.boards.utils.DeletePastWeekBoardsWorker
import com.example.finito.ui.theme.FinitoTheme
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        DynamicColors.applyToActivityIfAvailable(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setupDeletePastWeekBoardsWorker()
        setContent {
            FinitoTheme {
                App { finish() }
            }
        }
    }

    private fun setupDeletePastWeekBoardsWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()
        val deleteBoardsRequest = PeriodicWorkRequestBuilder<DeletePastWeekBoardsWorker>(
            DeletePastWeekBoardsWorker.MINIMUM_INTERVALS,
            TimeUnit.HOURS
        ).setConstraints(constraints).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DeletePastWeekBoardsWorker.TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            deleteBoardsRequest
        )
    }
}