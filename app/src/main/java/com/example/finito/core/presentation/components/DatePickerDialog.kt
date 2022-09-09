package com.example.finito.core.presentation.components

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate

@Composable
fun DatePickerDialog(
    initialDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onConfirmClick:() -> Unit = {},
    onDismissClick: () -> Unit = {},
) {
    val context = LocalContext.current
    LocalConfiguration.current.locales[0]
    val picker = MaterialDatePicker.Builder
        .datePicker()
        .setSelection(initialDate?.toEpochDay() ?: MaterialDatePicker.todayInUtcMilliseconds())
        .build()

    LaunchedEffect(Unit) {
        picker.show((context as AppCompatActivity).supportFragmentManager, picker.toString())
    }

    with(picker) {
        addOnDismissListener { onDismiss() }
        addOnNegativeButtonClickListener { onDismissClick() }
        addOnPositiveButtonClickListener { onConfirmClick() }
    }

//    MaterialDialog(
//        dialogState = dialogState,
//        shape = RoundedCornerShape(28.dp),
//        backgroundColor = finitoColors.surfaceColorAtElevation(elevation = 13.dp),
//        onCloseRequest = { onDismiss() },
//        buttons = {
//            positiveButton(
//                res = R.string.ok,
//                textStyle = MaterialTheme.typography.labelLarge.copy(
//                    color = finitoColors.primary,
//                ),
//                onClick = onConfirmClick
//            )
//            negativeButton(
//                res = R.string.cancel,
//                textStyle = MaterialTheme.typography.labelLarge.copy(
//                    color = finitoColors.primary,
//                ),
//                onClick = onDismissClick
//            )
//        }
//    ) {
//        datepicker(
//            locale = locale,
//            initialDate = initialDate ?: LocalDate.now(),
//            colors = DatePickerDefaults.colors(
//                headerBackgroundColor = finitoColors.primary,
//                headerTextColor = finitoColors.onPrimary,
//                dateActiveBackgroundColor = finitoColors.primary,
//                dateActiveTextColor = finitoColors.onPrimary,
//                dateInactiveTextColor = finitoColors.onSurface,
//                calendarHeaderTextColor = finitoColors.onSurface,
//            )
//        ) {}
}