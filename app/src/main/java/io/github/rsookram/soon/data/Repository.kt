package io.github.rsookram.soon.data

import androidx.datastore.core.DataStore
import io.github.rsookram.soon.Agenda
import io.github.rsookram.soon.Data
import io.github.rsookram.soon.Task
import io.github.rsookram.soon.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class Repository @Inject constructor(
    private val dataStore: DataStore<Data>,
    private val scheduler: Scheduler,
    private val clock: Clock,
) {

    val agenda: Flow<Agenda> = dataStore.data.map { it.agenda ?: defaultAgenda() }

    val tasks: Flow<List<Task>> = dataStore.data.map { it.tasks }

    suspend fun toggleComplete(todo: Todo) {
        dataStore.updateData { data ->
            data.copy(
                agenda = Agenda(
                    todos = (data.agenda ?: defaultAgenda()).todos.map {
                        if (it == todo) {
                            it.copy(isComplete = !it.isComplete)
                        } else {
                            it
                        }
                    }
                )
            )
        }
    }

    suspend fun addTask(task: Task) {
        dataStore.updateData { data ->
            data.copy(tasks = data.tasks + task)
        }
    }

    suspend fun removeTask(task: Task) {
        dataStore.updateData { data ->
            data.copy(tasks = data.tasks - task)
        }
    }

    suspend fun updateTask(oldTask: Task, newTask: Task) {
        dataStore.updateData { data ->
            data.copy(
                tasks = data.tasks.map {
                    if (it == oldTask) newTask else it
                }
            )
        }
    }

    suspend fun refreshAgenda() {
        dataStore.updateData { data ->
            val today = LocalDate.now(clock)

            val agenda = data.agenda ?: defaultAgenda()

            if (agenda.date == clock.todayAsSoonDate()) {
                // See if there's anything new to add to today
                val tasksForToday = data.tasks.filter { scheduler.shouldSchedule(it, today) }

                val existingTasks = agenda.todos.mapNotNull(Todo::task)
                val tasksToAdd = tasksForToday.filter { it !in existingTasks }

                data.copy(
                    agenda = agenda.copy(
                        todos = agenda.todos + tasksToAdd.map(::Todo)
                    )
                )
            } else {
                // Move incomplete tasks, and set tasks for current date
                val incompleteTasks = agenda.todos.filterNot(Todo::isComplete).mapNotNull(Todo::task)
                val (tasksToUpdate, tasksToReschedule) = incompleteTasks.partition { it.date != null }

                val todaySoonDate = today.toSoonDate()

                val updatedTasks =
                    data.tasks.filter { it !in tasksToUpdate } +
                            tasksToUpdate.map { it.copy(date = it.date!! + 1) } +
                            tasksToReschedule.map { Task(it.name, date = todaySoonDate) }

                data.copy(
                    agenda = Agenda(
                        date = todaySoonDate,
                        todos = updatedTasks.filter { scheduler.shouldSchedule(it, today) }.map(::Todo),
                    ),
                    tasks = updatedTasks,
                )
            }
        }
    }

    private fun defaultAgenda() = Agenda(date = clock.todayAsSoonDate())
}
