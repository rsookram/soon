package io.github.rsookram.soon.taskdetails

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import io.github.rsookram.soon.R
import io.github.rsookram.soon.Task
import io.github.rsookram.soon.ui.OverflowMenu
import java.time.LocalDate
import java.time.OffsetTime

@Composable
fun TaskDetails(
    task: Task,
    scheduledFor: String,
    initialDateSelection: LocalDate,
    onNameChange: (String) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
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
                FloatingActionButton(onConfirmClick) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.confirm_changes_to_task),
                    )
                }
            }
        },
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = task.name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text(stringResource(R.string.task_name)) },
            )

            Text(scheduledFor)

            // on date (date >= today)
            ScheduleOnDate(initialDateSelection, onDateSelect)

            // days of week
            Text(
                stringResource(R.string.schedule_by_day_of_week),
                Modifier
                    .fillMaxWidth()
                    .heightIn(56.dp)
                    .clickable { TODO() },
            )

            // n days from date (n >= 2, date >= today)
            Text(
                stringResource(R.string.schedule_every_n_days_from_date),
                Modifier
                    .fillMaxWidth()
                    .heightIn(56.dp)
                    .clickable { TODO() },
            )

            // nth day of month (1 - 28)
            ScheduleEveryNthDayOfMonth(onNthDayOfMonthSelect)
        }
    }
}

@Composable
private fun ScheduleOnDate(
    initialDateSelection: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
) {
    val context = LocalContext.current

    val onDatePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelect(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initialDateSelection.year,
            initialDateSelection.monthValue - 1,
            initialDateSelection.dayOfMonth,
        ).apply {
            datePicker.minDate =
                initialDateSelection.atTime(OffsetTime.MIN).toInstant().toEpochMilli()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onDatePickerDialog.dismiss()
        }
    }

    Text(
        stringResource(R.string.schedule_on_date),
        Modifier
            .fillMaxWidth()
            .heightIn(56.dp)
            .clickable { onDatePickerDialog.show() },
    )
}

@Composable
private fun ScheduleEveryNthDayOfMonth(onNthDayOfMonthSelect: (Int) -> Unit) {
    var showNthDayDialog by rememberSaveable { mutableStateOf(false) }

    Text(
        stringResource(R.string.schedule_on_nth_day_of_month),
        Modifier
            .fillMaxWidth()
            .heightIn(56.dp)
            .clickable { showNthDayDialog = true },
    )

    if (showNthDayDialog) {
        Dialog(onDismissRequest = { showNthDayDialog = false }) {
            val minDay = 1
            val maxDay = 28

            var n by remember { mutableStateOf(minDay) }

            Slider(
                value = n.toFloat(),
                onValueChange = { n = it.toInt() },
                valueRange = minDay.toFloat()..maxDay.toFloat(),
                // TODO: check for off by one error
                steps = maxDay - minDay,
                onValueChangeFinished = {
                    onNthDayOfMonthSelect(n)
                },
            )
        }
    }
}
