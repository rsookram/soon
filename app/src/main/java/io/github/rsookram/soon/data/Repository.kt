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
                agenda = (data.agenda ?: defaultAgenda()).copy(
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

        refreshAgenda()
    }

    suspend fun removeTask(task: Task) {
        dataStore.updateData { data ->
            data.copy(tasks = data.tasks - task)
        }

        refreshAgenda()
    }

    suspend fun updateTask(oldTask: Task, newTask: Task) {
        dataStore.updateData { data ->
            data.copy(
                tasks = data.tasks.map {
                    if (it == oldTask) newTask else it
                }
            )
        }

        refreshAgenda()
    }

    suspend fun refreshAgenda() {
        dataStore.updateData { data ->
            val today = clock.adjustedToday()

            val agenda = data.agenda ?: defaultAgenda()

            if (agenda.date == clock.todayAsSoonDate()) {
                // See if anything changed for today
                val tasksForToday = data.tasks.filter { scheduler.shouldSchedule(it, today) }

                val existingTodos = agenda.todos

                data.copy(
                    agenda = agenda.copy(
                        todos = tasksForToday.map { task ->
                            val existing = existingTodos.find { it.task == task }
                            Todo(task, existing?.isComplete ?: false)
                        }
                    )
                )
            } else {
                val (completeTodos, incompleteTodos) = agenda.todos.partition(Todo::isComplete)

                // These tasks were scheduled for a specific date and completed. Remove them from
                // the existing tasks.
                val finishedTasks = completeTodos.filter { it.task?.date != null }.map(Todo::task)

                // Incomplete tasks get scheduled for the next day.
                val incompleteTasks = incompleteTodos.mapNotNull(Todo::task)
                val (tasksToUpdate, tasksToReschedule) = incompleteTasks.partition { it.date != null }

                val todaySoonDate = today.toSoonDate()

                val updatedTasks =
                    data.tasks.filterNot { it in tasksToUpdate || it in finishedTasks } +
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
