package io.github.rsookram.soon.tasks

import android.content.Context
import android.icu.text.MessageFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import io.github.rsookram.soon.IntervalFromDate
import io.github.rsookram.soon.R
import io.github.rsookram.soon.Task
import io.github.rsookram.soon.data.contains
import io.github.rsookram.soon.data.toLocalDate
import io.github.rsookram.soon.ui.OverflowMenu
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun Tasks(tasks: List<Task>, onTaskClick: (Task) -> Unit, onNewTaskClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_of_scheduled_tasks_title)) },
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
                actions = {
                    val expanded = rememberSaveable { mutableStateOf(false) }

                    OverflowMenu(expanded) {
                        // TODO: import / export
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onNewTaskClick) {
                Icon(
                    Icons.Default.Create,
                    contentDescription = stringResource(R.string.create_new_task),
                )
            }
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.navigationBars,
                )
            ) {
                items(tasks) { task ->
                    TaskRow(
                        Modifier
                            .clickable { onTaskClick(task) }
                            .fillMaxWidth(),
                        task,
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(modifier: Modifier = Modifier, task: Task) {
    val context = LocalContext.current

    Column(
        modifier
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(task.name, style = MaterialTheme.typography.body1)

        Text(
            task.localizedSchedule(context),
            Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.caption
        )
    }
}

private fun Task.localizedSchedule(context: Context): String =
    when {
        date != null -> {
            date.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        }
        daysOfWeek != null -> {
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

            val week = (DayOfWeek.values() + DayOfWeek.values()).dropWhile { it != firstDayOfWeek }.take(7)

            val days = week.filter { it in daysOfWeek }

            days.joinToString(separator = context.getString(R.string.day_of_week_separator)) {
                it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
        }
        nDaysFromDate != null -> {
            context.resources.getQuantityString(
                R.plurals.every_n_days_from_date,
                nDaysFromDate.interval,
                nDaysFromDate.interval,
                nDaysFromDate.date.toLocalDate()
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            )
        }
        nthDayOfMonth != null -> {
            val ordinal = MessageFormat.format("{0,ordinal}", nthDayOfMonth)
            context.resources.getString(R.string.every_nth_day_of_month, ordinal)
        }
        else -> {
            throw IllegalStateException("Unknown schedule for $this")
        }
    }

@Preview
@Composable
fun TaskRowPreview() {
    Column {
        TaskRow(task = Task("First", date = 20))

        TaskRow(task = Task("Second", daysOfWeek = 1))

        TaskRow(task = Task("Third", nDaysFromDate = IntervalFromDate(interval = 3, 2)))

        TaskRow(task = Task("Fourth", nthDayOfMonth = 2))
    }
}
