package io.github.rsookram.soon.data

import io.github.rsookram.soon.Task
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

/**
 * Bitfield representing a set of days of the week.
 */
typealias DaysOfWeek = Int

class Scheduler @Inject constructor() {

    fun shouldSchedule(task: Task, date: LocalDate): Boolean =
        if (task.date != null) {
            task.date == date.toSoonDate()
        } else if (task.daysOfWeek != null) {
            date.dayOfWeek in task.daysOfWeek
        } else if (task.nDaysFromDate != null) {
            val nDaysFromDate = task.nDaysFromDate
            val fromDate = nDaysFromDate.date

            val soonDate = date.toSoonDate()

            if (fromDate > soonDate) {
                false
            } else {
                (soonDate - fromDate) % nDaysFromDate.interval == 0
            }
        } else if (task.nthDayOfMonth != null) {
            task.nthDayOfMonth == date.dayOfMonth
        } else {
            throw IllegalStateException("Unknown schedule for $task")
        }
}

operator fun DaysOfWeek.contains(dayOfWeek: DayOfWeek): Boolean =
    this and dayOfWeek.toBitmask() > 0

fun DaysOfWeek.toEnumSet(): EnumSet<DayOfWeek> {
    val result = EnumSet.noneOf(DayOfWeek::class.java)

    DayOfWeek.values().forEach { day ->
        if (day in this) {
            result.add(day)
        }
    }

    return result
}

fun EnumSet<DayOfWeek>.toSoonDaysOfWeek(): Int {
    var result = 0

    forEach { day ->
        result = result or day.toBitmask()
    }

    return result
}

private fun DayOfWeek.toBitmask() = 1 shl (value - 1)
