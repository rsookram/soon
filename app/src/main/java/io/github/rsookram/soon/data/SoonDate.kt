package io.github.rsookram.soon.data

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// TODO: Check if Wire can use UInt

/**
 * See [adjustedToday]
 */
private const val START_OF_DAY_HOUR = 4

/**
 * A "soon date" is measured as the number of days since the development of this app started
 * (2021/12/17). This value is represented as a non-negative Int, with 2021/12/17 being 0.
 */
typealias SoonDate = Int

fun Clock.todayAsSoonDate(): SoonDate = adjustedToday().toSoonDate()

/**
 * Returns the [LocalDate] for today, assuming that the day starts at [START_OF_DAY_HOUR].
 */
fun Clock.adjustedToday(): LocalDate {
    val now = LocalDateTime.now(this)
    val shouldPushBack = now.hour < START_OF_DAY_HOUR
    return now.toLocalDate().minusDays(if (shouldPushBack) 1 else 0)
}

fun LocalDate.toSoonDate(): SoonDate {
    val epoch = LocalDate.of(2021, 12, 17)

    // TODO: Error if overflow
    return ChronoUnit.DAYS.between(epoch, this).toInt()
}

fun SoonDate.toLocalDate(): LocalDate {
    val epoch = LocalDate.of(2021, 12, 17)
    return epoch.plusDays(this.toLong())
}
