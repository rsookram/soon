package io.github.rsookram.soon.taskdetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.soon.ApplicationScope
import io.github.rsookram.soon.Task
import io.github.rsookram.soon.data.Repository
import io.github.rsookram.soon.data.todayAsSoonDate
import io.github.rsookram.soon.tasks.localizedSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeBase64
import java.time.Clock
import javax.inject.Inject

/**
 * ViewModel for [TaskDetails]
 */
@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val repository: Repository,
    clock: Clock,
    savedStateHandle: SavedStateHandle,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    val isCreation: Boolean
    val task: Task
        get() = _task.value
    // TODO: Save in saved state handle to handle proc death
    private val _task: MutableState<Task>

    init {
        val bytes = savedStateHandle.get<String>("base64")?.decodeBase64()
        isCreation = bytes == null

        _task = mutableStateOf(
            if (bytes != null) {
                Task.ADAPTER.decode(bytes)
            } else {
                Task(name = "", date = clock.todayAsSoonDate() + 1)
            }
        )
    }

    fun onNameChange(newName: String) {
        _task.value = task.copy(name = newName)
    }

    fun onConfirmClick() {
        applicationScope.launch {
            if (isCreation) {
                // TODO: create
            } else {
                // TODO: save
            }
        }
    }

    fun onDeleteClick() {
        applicationScope.launch {
            repository.removeTask(task)
        }
    }
}

/**
 * The stateful version of [TaskDetails]
 */
@Composable
fun TaskDetails(navController: NavController, vm: TaskDetailsViewModel = hiltViewModel()) {
    val context = LocalContext.current

    TaskDetails(
        vm.task,
        vm.task.localizedSchedule(context),
        onNameChange = vm::onNameChange,
        onUpClick = { navController.popBackStack() },
        // TODO: Pop on confirm / delete
        onConfirmClick = if (vm.task.name.isNotBlank()) vm::onConfirmClick else null,
        onDeleteClick = if (vm.isCreation) vm::onDeleteClick else null,
    )
}
