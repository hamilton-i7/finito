package com.example.finito.core.presentation.components.dialogs

import android.text.format.DateFormat.is24HourFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime

@Composable
fun TimePickerDialog(
    initialTime: LocalTime? = null,
    onDismiss: () -> Unit,
    onConfirmClick:(LocalTime) -> Unit = {},
    onDismissClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val time = initialTime ?: LocalTime.now().run {
        // Set default time to be either o'clock or half hour
        if (minute > 30) {
            LocalTime.of(plusHours(1).hour, 0)
        } else {
            LocalTime.of(hour, 30)
        }
    }
    val picker = MaterialTimePicker.Builder()
        .setTimeFormat(if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
        .setHour(time.hour)
        .setMinute(time.minute)
        .build()

    LaunchedEffect(Unit) {
        picker.show((context as AppCompatActivity).supportFragmentManager, picker.toString())
    }

    with(picker) {
        addOnDismissListener { onDismiss() }
        addOnNegativeButtonClickListener { onDismissClick() }
        addOnPositiveButtonClickListener {
            onConfirmClick(LocalTime.of(picker.hour, picker.minute))
        }
    }
}