package io.github.rsookram.soon.taskdetails

import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.insets.ui.TopAppBar
import io.github.rsookram.soon.R
import io.github.rsookram.soon.Task
import io.github.rsookram.soon.data.DaysOfWeek
import io.github.rsookram.soon.data.SoonDate
import io.github.rsookram.soon.data.toEnumSet
import io.github.rsookram.soon.data.toLocalDate
import io.github.rsookram.soon.tasks.getLocalizedWeek
import io.github.rsookram.soon.ui.OverflowMenu
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetTime
import java.time.format.TextStyle
import java.util.*

enum class ScheduleType {
    ON_DATE,
    BY_DAY_OF_WEEK,
    NTH_DAY_OF_MONTH,
}

@Composable
fun TaskDetails(
    task: Task,
    scheduledFor: String,
    defaultDateSelection: LocalDate,
    onNameChange: (String) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onDaysOfWeekSelect: (EnumSet<DayOfWeek>) -> Unit,
    onNthDayOfMonthSelect: (Int) -> Unit,
    onUpClick: () -> Unit,
    onConfirmClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
                navigationIcon = {
                    IconButton(onClick = onUpClick) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.toolbar_up_description),
                        )
                    }
                },
                actions = {
                    if (onDeleteClick != null) {
                        val expanded = rememberSaveable { mutableStateOf(false) }

                        OverflowMenu(expanded) {
                            DropdownMenuItem(
                                onClick = {
                                    expanded.value = false
                                    onDeleteClick()
                                }
                            ) {
                                Text(stringResource(R.string.delete_task))
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (onConfirmClick != null) {
                FloatingActionButton(
                    onConfirmClick,
                    Modifier.navigationBarsWithImePadding(),
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.confirm_changes_to_task),
                    )
                }
            }
        },
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .systemBarsPadding(top = false)
                .padding(bottom = 56.dp), // protection from the FAB
        ) {
            OutlinedTextField(
                value = task.name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text(stringResource(R.string.task_name)) },
            )

            Text(
                scheduledFor,
                Modifier.padding(16.dp),
                style = MaterialTheme.typography.h6,
            )

            var selectedOption by rememberSaveable { mutableStateOf(task.scheduleType()) }

            ScheduleOptions(selectedOption, onSelect = { selectedOption = it })

            when (selectedOption) {
                ScheduleType.ON_DATE ->
                    ScheduleOnDate(task.date, defaultDateSelection, onDateSelect)
                ScheduleType.BY_DAY_OF_WEEK ->
                    ScheduleOnDaysOfWeek(task.daysOfWeek, onDaysOfWeekSelect)
                ScheduleType.NTH_DAY_OF_MONTH ->
                    ScheduleEveryNthDayOfMonth(task.nthDayOfMonth, onNthDayOfMonthSelect)
            }
        }
    }
}

private fun Task.scheduleType(): ScheduleType =
    when {
        date != null -> ScheduleType.ON_DATE
        daysOfWeek != null -> ScheduleType.BY_DAY_OF_WEEK
        nthDayOfMonth != null -> ScheduleType.NTH_DAY_OF_MONTH
        else -> error("Unknown schedule for $this")
    }

@Composable
private fun ScheduleOptions(scheduleType: ScheduleType, onSelect: (ScheduleType) -> Unit) {
    ScheduleType.values().forEach { type ->
        Row(
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = scheduleType == type,
                    onClick = { onSelect(type) },
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = scheduleType == type,
                onClick = { onSelect(type) },
            )
            Text(
                text = stringResource(
                    when (type) {
                        ScheduleType.ON_DATE -> R.string.schedule_on_date
                        ScheduleType.BY_DAY_OF_WEEK -> R.string.schedule_by_day_of_week
                        ScheduleType.NTH_DAY_OF_MONTH -> R.string.schedule_on_nth_day_of_month
                    }
                ),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun ScheduleOnDate(
    taskDate: SoonDate?,
    defaultDateSelection: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
) {
    val initialDate = taskDate?.toLocalDate() ?: defaultDateSelection

    Box(Modifier.fillMaxWidth()) {
        AndroidView(
            ::DatePicker,
            Modifier.align(Alignment.Center),
        ) { datePicker ->
            datePicker.apply {
                minDate =
                    defaultDateSelection.atTime(OffsetTime.MIN).toInstant().toEpochMilli()

                init(
                    initialDate.year,
                    initialDate.monthValue - 1,
                    initialDate.dayOfMonth
                ) { _, year, month, dayOfMonth ->
                    onDateSelect(LocalDate.of(year, month + 1, dayOfMonth))
                }
            }
        }
    }
}

@Composable
private fun ScheduleOnDaysOfWeek(
    taskDaysOfWeek: DaysOfWeek?,
    onDaysOfWeekSelect: (EnumSet<DayOfWeek>) -> Unit,
) {
    val selectedDays = taskDaysOfWeek?.toEnumSet() ?: EnumSet.noneOf(DayOfWeek::class.java)

    Week(
        selectedDays,
        onDayClick = { day ->
            selectedDays.toggle(day)
            onDaysOfWeekSelect(selectedDays)
        },
    )
}

private fun EnumSet<DayOfWeek>.toggle(day: DayOfWeek) {
    if (!add(day)) {
        remove(day)
    }
}

@Composable
private fun Week(selectedDays: EnumSet<DayOfWeek>, onDayClick: (DayOfWeek) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        getLocalizedWeek().forEach { day ->
            Text(
                day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                Modifier
                    .clickable { onDayClick(day) }
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .background(
                        if (day in selectedDays) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Transparent
                        }
                    ),
                color = if (day in selectedDays) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun WeekPreview() {
    Box(Modifier.background(MaterialTheme.colors.surface)) {
        Week(
            selectedDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
            onDayClick = {},
        )
    }
}

@Composable
private fun ScheduleEveryNthDayOfMonth(
    taskNthDayOfMonth: Int?,
    onNthDayOfMonthSelect: (Int) -> Unit
) {
    val minDay = 1
    val maxDay = 28

    var n by remember { mutableStateOf(taskNthDayOfMonth ?: minDay) }

    Slider(
        value = n.toFloat(),
        onValueChange = { n = it.toInt() },
        modifier = Modifier.padding(horizontal = 16.dp),
        valueRange = minDay.toFloat()..maxDay.toFloat(),
        // TODO: check for off by one error
        steps = maxDay - minDay,
        onValueChangeFinished = {
            onNthDayOfMonthSelect(n)
        },
    )
}
