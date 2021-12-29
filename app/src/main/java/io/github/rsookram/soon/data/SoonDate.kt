package io.github.rsookram.soon.data

import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// TODO: Check if Wire can use UInt

/**
 * A "soon date" is measured as the number of days since the development of this app started
 * (2021/12/17). This value is represented as a non-negative Int, with 2021/12/17 being 0.
 */
typealias SoonDate = Int

fun Clock.todayAsSoonDate(): SoonDate = LocalDate.now(this).toSoonDate()

fun LocalDate.toSoonDate(): SoonDate {
    val epoch = LocalDate.of(2021, 12, 17)

    // TODO: Error if overflow
    return ChronoUnit.DAYS.between(epoch, this).toInt()
}

fun SoonDate.toLocalDate(): LocalDate {
    val epoch = LocalDate.of(2021, 12, 17)
    return epoch.plusDays(this.toLong())
}
