package com.example.finito.core.presentation.components.dialogs

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun DatePickerDialog(
    initialDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onConfirmClick:(LocalDate) -> Unit = {},
    onDismissClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val dateInMillis = initialDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    val picker = MaterialDatePicker.Builder
        .datePicker()
        .setSelection(dateInMillis ?: MaterialDatePicker.todayInUtcMilliseconds())
        .build()

    LaunchedEffect(Unit) {
        picker.show((context as AppCompatActivity).supportFragmentManager, picker.toString())
    }

    with(picker) {
        addOnDismissListener { onDismiss() }
        addOnNegativeButtonClickListener { onDismissClick() }
        addOnPositiveButtonClickListener {
            val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
            onConfirmClick(date)
        }
    }
}