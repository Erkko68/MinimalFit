package eric.bitria.minimalfit.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

private val shortMonthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private val shortWeekdayNames = mapOf(
    DayOfWeek.MONDAY to "MON",
    DayOfWeek.TUESDAY to "TUE",
    DayOfWeek.WEDNESDAY to "WED",
    DayOfWeek.THURSDAY to "THU",
    DayOfWeek.FRIDAY to "FRI",
    DayOfWeek.SATURDAY to "SAT",
    DayOfWeek.SUNDAY to "SUN"
)

private val fullWeekdayNames = mapOf(
    DayOfWeek.MONDAY to "Monday",
    DayOfWeek.TUESDAY to "Tuesday",
    DayOfWeek.WEDNESDAY to "Wednesday",
    DayOfWeek.THURSDAY to "Thursday",
    DayOfWeek.FRIDAY to "Friday",
    DayOfWeek.SATURDAY to "Saturday",
    DayOfWeek.SUNDAY to "Sunday"
)

private fun LocalDate.monthIndex(): Int = month.ordinal

fun nowInstant(): Instant = Clock.System.now()

fun nowDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    nowInstant().toLocalDateTime(timeZone)

fun today(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    nowDateTime(timeZone).date

fun currentDateAndTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<LocalDate, LocalTime> {
    val now = nowDateTime(timeZone)
    return now.date to now.time
}

fun LocalDate.shortMonthDay(): String = "${shortMonthNames[monthIndex()]} $day"

fun LocalDate.monthDayYear(): String = "${shortMonthNames[monthIndex()]} $day, $year"

fun LocalDate.weekdayMonthDay(): String = "${fullWeekdayName()}, ${shortMonthNames[monthIndex()]} $day"

fun LocalDate.shortWeekdayLabel(): String = shortWeekdayNames[dayOfWeek] ?: "---"

fun last7DaysEndingToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<LocalDate> {
    val currentDay = today(timeZone)
    return (6 downTo 0).map { currentDay.minus(it.toLong(), DateTimeUnit.DAY) }
}

fun LocalDate.fullWeekdayName(): String = fullWeekdayNames[dayOfWeek] ?: "Unknown"

fun LocalTime.hourMinute(): String = "%02d:%02d".format(hour, minute)
