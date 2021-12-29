package io.github.rsookram.soon.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.soon.data.Repository
import javax.inject.Inject

/**
 * ViewModel for [Tasks]
 */
@HiltViewModel
class TasksViewModel @Inject constructor(repository: Repository) : ViewModel() {

    val tasks = repository.tasks
}

/**
 * The stateful version of [Tasks]
 */
@Composable
fun Tasks(navController: NavController, vm: TasksViewModel = hiltViewModel()) {
    Tasks(
        tasks = vm.tasks.collectAsState(initial = emptyList()).value,
        onTaskClick = {},
        onNewTaskClick = {},
    )
}
