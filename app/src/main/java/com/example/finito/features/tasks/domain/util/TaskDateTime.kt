package com.example.finito.features.tasks.domain.util

import android.content.Context
import com.example.finito.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.WeekFields
import java.util.*

fun toFormattedChipDate(
    date: LocalDate,
    time: LocalTime?,
    context: Context,
    locale: Locale,
): String {
    val today = LocalDate.now()
    val formattedDate = if (date.isAfter(today)) {
        date.toFutureFormat(today, context, locale)
    } else if (date.isEqual(today)) {
        context.getString(R.string.today)
    } else date.toPastFormat(today, context, locale)

    return setupResult(formattedDate, time)
}

private fun setupResult(
    formattedDate: String,
    time: LocalTime?,
): String {
    return time?.let { "$formattedDate - ${it.formatted()}" } ?: formattedDate
}

private fun LocalDate.toFutureFormat(today: LocalDate, context: Context, locale: Locale): String {
    val result: String = when {
        isTomorrow(today) -> context.getString(R.string.tomorrow)
        isNextWeek(today, locale) -> context.getString(R.string.next_week)
        isNextMonth(today) -> context.getString(R.string.next_month)
        isCurrentYear(today) -> toCurrentYearFormat(locale)
        else -> toFullFormat(locale)
    }
    return result
}

private fun LocalDate.toPastFormat(today: LocalDate, context: Context, locale: Locale): String {
    val result: String = when {
        isDaysAgo(today, days = 1) -> context.getString(R.string.yesterday)
        isDaysAgo(today, days = 2) -> context.getString(R.string.two_days_ago)
        isDaysAgo(today, days = 3) -> context.getString(R.string.three_days_ago)
        isDaysAgo(today, days = 4) -> context.getString(R.string.four_days_ago)
        isDaysAgo(today, days = 5) -> context.getString(R.string.five_days_ago)
        isDaysAgo(today, days = 6) -> context.getString(R.string.six_days_ago)
        isLastWeek(today, locale) -> context.getString(R.string.last_week)
        isTwoWeeksAgo(today, locale) -> context.getString(R.string.two_weeks_ago)
        isLastMonth(today) -> context.getString(R.string.last_month)
        isCurrentYear(today) -> toCurrentYearFormat(locale)
        else -> toFullFormat(locale)
    }
    return result
}

@Suppress("SpellCheckingInspection")
fun LocalDate.toCurrentYearFormat(locale: Locale, complete: Boolean = false): String {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
        if (complete) "cccc LLLL d" else "ccc LLL d",
        locale
    )
    return formatter.format(this)
}

@Suppress("SpellCheckingInspection")
fun LocalDate.toFullFormat(locale: Locale, complete: Boolean = false): String {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
        if (complete) "cccc LLLL d, yyyy" else "ccc LLL d, yyyy",
        locale
    )
    return formatter.format(this)
}

fun LocalTime.formatted(): String {
    return format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
}

fun LocalDateTime.isPast(): Boolean {
    val now = LocalDateTime.now()
    if (toLocalDate().isBefore(now.toLocalDate())) return true
    if (toLocalDate().isAfter(now.toLocalDate())) return false
    if (hour < now.hour) return true
    if (hour > now.hour) return false
    return minute < now.minute
}

private fun LocalDate.isTomorrow(today: LocalDate): Boolean = isEqual(today.plusDays(1))

private fun LocalDate.isDaysAgo(today: LocalDate, days: Long): Boolean = isEqual(today.minusDays(days))

private fun LocalDate.isNextWeek(today: LocalDate, locale: Locale): Boolean {
    val selectedWeekNumber = get(WeekFields.of(locale).weekOfWeekBasedYear())
    val currentWeekNumber = today.get(WeekFields.of(locale).weekOfWeekBasedYear())
    val nextWeekNumber = currentWeekNumber + 1
    return selectedWeekNumber == nextWeekNumber
}

private fun LocalDate.isLastWeek(today: LocalDate, locale: Locale): Boolean {
    val selectedWeekNumber = get(WeekFields.of(locale).weekOfWeekBasedYear())
    val currentWeekNumber = today.get(WeekFields.of(locale).weekOfWeekBasedYear())
    val lastWeekNumber = currentWeekNumber - 1
    return selectedWeekNumber == lastWeekNumber
}

private fun LocalDate.isTwoWeeksAgo(today: LocalDate, locale: Locale): Boolean {
    val selectedWeekNumber = get(WeekFields.of(locale).weekOfWeekBasedYear())
    val currentWeekNumber = today.get(WeekFields.of(locale).weekOfWeekBasedYear())
    val last2WeeksNumber = currentWeekNumber - 2
    return selectedWeekNumber == last2WeeksNumber
}

private fun LocalDate.isNextMonth(today: LocalDate): Boolean {
    val nextMonth = today.monthValue + 1
    return monthValue == nextMonth
}

private fun LocalDate.isLastMonth(today: LocalDate): Boolean {
    val lastMonth = today.monthValue - 1
    return monthValue == lastMonth
}

fun LocalDate.isCurrentYear(today: LocalDate): Boolean = year == today.year